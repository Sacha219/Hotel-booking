import axios from 'axios'
import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
})

const hotelPhoto =
  'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1400&q=80'
const roomPhoto =
  'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80'
const poolPhoto =
  'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=900&q=80'
const cityPhoto =
  'https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=900&q=80'
const suitePhoto =
  'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=900&q=80'
const loungePhoto =
  'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=900&q=80'
const defaultHotelImages = [hotelPhoto, poolPhoto, cityPhoto]
const defaultRoomImages = [roomPhoto, suitePhoto, loungePhoto]

const SESSION_KEY = 'royalstay_backend_session'
const LEGACY_SESSION_KEY = 'royalstay_session'
const LEGACY_USERS_KEY = 'royalstay_users'
const FAVORITES_KEY = 'royalstay_favorite_hotels'
const BOOKING_EXTRAS_KEY = 'royalstay_booking_extras'

const today = new Date()
const formatDateInput = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const addDays = (days) => {
  const date = new Date(today)
  date.setDate(date.getDate() + days)
  return formatDateInput(date)
}

const addDaysToDate = (dateString, days) => {
  const date = new Date(`${dateString}T00:00:00`)
  date.setDate(date.getDate() + days)
  return formatDateInput(date)
}

const isDateRangeValid = (checkIn, checkOut) =>
  Boolean(checkIn && checkOut && checkOut > checkIn)

const getDisplayBookingDates = (booking) => {
  const checkInDate = booking?.checkInDate
  const checkOutDate = booking?.checkOutDate

  if (checkInDate && checkOutDate && checkOutDate < checkInDate) {
    return {
      checkInDate: checkOutDate,
      checkOutDate: checkInDate,
      corrected: true,
    }
  }

  return {
    checkInDate,
    checkOutDate,
    corrected: false,
  }
}

const getBookingDisplayTotal = (booking) => {
  const totalPrice = Number(booking?.totalPrice || 0)
  return Math.abs(totalPrice)
}

const emptyHotel = {
  id: null,
  name: '',
  address: '',
  city: '',
  stars: 4,
  description: '',
  imageUrl: '',
  pricePerNight: 120,
  available: true,
  amenityIds: [],
}

const emptyRoom = {
  id: null,
  number: '',
  floor: 1,
  capacity: 2,
  type: 'STANDARD',
  price: 120,
  imageUrl: '',
  available: true,
  hotelId: '',
  amenityIds: [],
}

const emptyGuest = {
  id: null,
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
}

const emptyAmenity = {
  id: null,
  name: '',
  description: '',
  icon: 'wifi',
}

const adminTabs = [
  { id: 'hotels', label: 'Отели' },
  { id: 'rooms', label: 'Номера' },
  { id: 'bookings', label: 'Брони' },
  { id: 'guests', label: 'Гости' },
  { id: 'amenities', label: 'Удобства' },
]

const userTabs = [
  { id: 'search', label: 'Отели' },
  { id: 'myBookings', label: 'Бронирование' },
  { id: 'favorites', label: 'Избранное' },
]

const roomTypes = ['STANDARD', 'DELUXE', 'SUITE']
const bookingStatuses = ['PENDING', 'CONFIRMED', 'CANCELLED']

const roomTypeLabels = {
  STANDARD: 'Стандарт',
  DELUXE: 'Делюкс',
  SUITE: 'Люкс',
}

const bookingStatusLabels = {
  PENDING: 'Ожидает',
  CONFIRMED: 'Подтверждено',
  CANCELLED: 'Отменено',
}

const HOTEL_PAGE_SIZE = 4
const ADMIN_PAGE_SIZE = 6

function readJson(key, fallback) {
  try {
    const value = localStorage.getItem(key)
    return value ? JSON.parse(value) : fallback
  } catch {
    return fallback
  }
}

function saveSession(user) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(user))
}

function toNumber(value, fallback = null) {
  if (value === '' || value === null || value === undefined) {
    return fallback
  }
  return Number(value)
}

function extractError(error) {
  const data = error?.response?.data
  if (typeof data === 'string') {
    return data.replaceAll('CANCELLED', 'Отменено')
  }
  return String(data?.message || data?.error || error?.message || 'Не удалось выполнить запрос').replaceAll(
    'CANCELLED',
    'Отменено',
  )
}

function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  )
}

function EmptyState({ title }) {
  return <div className="empty-state">{title}</div>
}

function StatusPill({ active, tone, children }) {
  const toneClass = tone ? `pill-${tone}` : active ? 'pill-success' : ''
  return <span className={['pill', toneClass].filter(Boolean).join(' ')}>{children}</span>
}

function PasswordInput({ name, placeholder, required, value, visible, onChange, onToggle }) {
  return (
    <div className="password-input">
      <input
        name={name}
        placeholder={placeholder}
        required={required}
        type={visible ? 'text' : 'password'}
        value={value}
        onChange={onChange}
      />
      <button
        className="password-toggle"
        type="button"
        aria-label={visible ? 'Скрыть пароль' : 'Показать пароль'}
        title={visible ? 'Скрыть пароль' : 'Показать пароль'}
        onClick={onToggle}
      >
        {visible ? (
          <svg aria-hidden="true" viewBox="0 0 24 24">
            <path d="M3 3l18 18" />
            <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
            <path d="M9.5 5.3A9.7 9.7 0 0 1 12 5c5 0 8.6 4.2 10 7a15.1 15.1 0 0 1-3.1 4.2" />
            <path d="M6.6 6.6A15.2 15.2 0 0 0 2 12c1.4 2.8 5 7 10 7a9.7 9.7 0 0 0 4.1-.9" />
          </svg>
        ) : (
          <svg aria-hidden="true" viewBox="0 0 24 24">
            <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        )}
      </button>
    </div>
  )
}

function Stars({ value = 0 }) {
  return <span className="stars">{'★'.repeat(Number(value) || 0)}</span>
}

function getPagedItems(items, page, pageSize) {
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize))
  const safePage = Math.min(Math.max(page, 1), totalPages)
  const start = (safePage - 1) * pageSize

  return {
    items: items.slice(start, start + pageSize),
    page: safePage,
    totalPages,
  }
}

function Pagination({ page, pageSize, totalItems, onPageChange }) {
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))

  if (totalPages <= 1) {
    return null
  }

  const safePage = Math.min(Math.max(page, 1), totalPages)
  const pages = Array.from({ length: totalPages }, (_, index) => index + 1)

  return (
    <nav className="pagination" aria-label="Пагинация">
      <button
        className="ghost-button"
        type="button"
        disabled={safePage === 1}
        onClick={() => onPageChange(safePage - 1)}
      >
        Назад
      </button>
      <div className="pagination-pages">
        {pages.map((pageNumber) => (
          <button
            key={pageNumber}
            className={pageNumber === safePage ? 'pagination-page active' : 'pagination-page'}
            type="button"
            aria-current={pageNumber === safePage ? 'page' : undefined}
            onClick={() => onPageChange(pageNumber)}
          >
            {pageNumber}
          </button>
        ))}
      </div>
      <button
        className="ghost-button"
        type="button"
        disabled={safePage === totalPages}
        onClick={() => onPageChange(safePage + 1)}
      >
        Вперед
      </button>
    </nav>
  )
}

function Notification({ type = 'info', message, onClose }) {
  if (!message) {
    return null
  }

  return (
    <div className={`toast-notice ${type}`} role="alert">
      <span>{message}</span>
      <button type="button" aria-label="Закрыть уведомление" onClick={onClose}>
        ×
      </button>
    </div>
  )
}

function roomTypeLabel(type) {
  return roomTypeLabels[type] || type || 'Номер'
}

function bookingStatusLabel(status) {
  return bookingStatusLabels[status] || String(status || 'Статус не указан').replaceAll('CANCELLED', 'Отменено')
}

function bookingStatusTone(status) {
  if (status === 'CONFIRMED') {
    return 'success'
  }

  if (status === 'CANCELLED') {
    return 'danger'
  }

  return 'warning'
}

function formatPrice(value) {
  return `${Number(value || 0).toLocaleString('ru-RU')} руб.`
}

function normalizeSearch(value) {
  return String(value || '').trim().toLowerCase()
}

function compareText(first, second) {
  return String(first || '').localeCompare(String(second || ''), 'ru')
}

function compareRoomNumber(first, second) {
  return String(first || '').localeCompare(String(second || ''), 'ru', {
    numeric: true,
    sensitivity: 'base',
  })
}

function matchesQuery(parts, query) {
  if (!query) {
    return true
  }

  return parts.some((part) => normalizeSearch(part).includes(query))
}

function guestCountLabel(value) {
  const count = Math.max(1, Number(value) || 1)
  const lastDigit = count % 10
  const lastTwoDigits = count % 100

  if (lastDigit === 1 && lastTwoDigits !== 11) {
    return `${count} гость`
  }

  if ([2, 3, 4].includes(lastDigit) && ![12, 13, 14].includes(lastTwoDigits)) {
    return `${count} гостя`
  }

  return `${count} гостей`
}

function getBookingSortRank(booking) {
  return Number(booking?.id) || Date.parse(`${booking?.checkInDate || ''}T00:00:00`) || 0
}

function isBookingInactive(booking) {
  const dates = getDisplayBookingDates(booking)
  const todayValue = formatDateInput(today)

  return booking?.status === 'CANCELLED' || Boolean(dates.checkOutDate && dates.checkOutDate < todayValue)
}

function parseImageUrls(value) {
  if (Array.isArray(value)) {
    return value.filter(Boolean)
  }

  if (!value || typeof value !== 'string') {
    return []
  }

  const trimmed = value.trim()
  if (!trimmed) {
    return []
  }

  if (trimmed.startsWith('[')) {
    try {
      const parsed = JSON.parse(trimmed)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : []
    } catch {
      return [trimmed]
    }
  }

  return [trimmed]
}

function serializeImageUrls(images) {
  const prepared = images.filter(Boolean)
  return prepared.length ? JSON.stringify(prepared) : ''
}

function App() {
  const [authUser, setAuthUser] = useState(() => readJson(SESSION_KEY, null))
  const [authMode, setAuthMode] = useState('login')
  const [authError, setAuthError] = useState('')
  const [visiblePasswords, setVisiblePasswords] = useState({
    login: false,
    register: false,
    confirm: false,
  })
  const [loginForm, setLoginForm] = useState({
    email: '',
    password: '',
  })
  const [registerForm, setRegisterForm] = useState({
    firstName: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [activeTab, setActiveTab] = useState(() =>
    readJson(SESSION_KEY, null)?.role === 'ADMIN' ? 'hotels' : 'search',
  )
  const [hotels, setHotels] = useState([])
  const [rooms, setRooms] = useState([])
  const [guests, setGuests] = useState([])
  const [bookings, setBookings] = useState([])
  const [amenities, setAmenities] = useState([])
  const [loading, setLoading] = useState(() => Boolean(readJson(SESSION_KEY, null)))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [hotelForm, setHotelForm] = useState(emptyHotel)
  const [roomForm, setRoomForm] = useState(emptyRoom)
  const [hotelModalOpen, setHotelModalOpen] = useState(false)
  const [roomModalOpen, setRoomModalOpen] = useState(false)
  const [guestForm, setGuestForm] = useState(emptyGuest)
  const [amenityForm, setAmenityForm] = useState(emptyAmenity)
  const [amenityModalOpen, setAmenityModalOpen] = useState(false)
  const [hotelFilters, setHotelFilters] = useState({
    city: '',
    stars: '',
    roomType: 'STANDARD',
    minPrice: 80,
    checkInDate: addDays(0),
    checkOutDate: addDays(1),
    guests: 2,
  })
  const [availabilityForm, setAvailabilityForm] = useState({
    hotelId: '',
    checkIn: addDays(0),
    checkOut: addDays(1),
  })
  const [adminAvailabilityFilter, setAdminAvailabilityFilter] = useState(null)
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false)
  const [selectedSearchHotelId, setSelectedSearchHotelId] = useState('')
  const [roomBrowserOpen, setRoomBrowserOpen] = useState(false)
  const [userRoomFilters, setUserRoomFilters] = useState({
    type: '',
    maxPrice: '',
    guests: '',
  })
  const [guestEmailQuery, setGuestEmailQuery] = useState('')
  const [adminHotelQuery, setAdminHotelQuery] = useState('')
  const [adminHotelSort, setAdminHotelSort] = useState('name')
  const [adminBookingQuery, setAdminBookingQuery] = useState('')
  const [adminBookingSort, setAdminBookingSort] = useState('newest')
  const [userRoomSort, setUserRoomSort] = useState('priceAsc')
  const [favoriteHotelIds, setFavoriteHotelIds] = useState(() => readJson(FAVORITES_KEY, []))
  const [bookingExtras, setBookingExtras] = useState(() => readJson(BOOKING_EXTRAS_KEY, {}))
  const [bookingModalRoomId, setBookingModalRoomId] = useState(null)
  const [expandedImage, setExpandedImage] = useState(null)
  const [imageIndexes, setImageIndexes] = useState({})
  const [bookingGuestForm, setBookingGuestForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    guests: 1,
    paymentMethod: 'at_hotel',
  })
  const [pages, setPages] = useState({
    hotels: 1,
    rooms: 1,
    bookings: 1,
    guests: 1,
    amenities: 1,
    myBookings: 1,
    favorites: 1,
    userRooms: 1,
  })

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [hotelRes, roomRes, guestRes, bookingRes, amenityRes] =
        await Promise.all([
          api.get('/hotels'),
          api.get('/rooms'),
          api.get('/guests'),
          api.get('/bookings'),
          api.get('/amenities'),
        ])

      setHotels(hotelRes.data)
      setRooms(roomRes.data)
      setGuests(guestRes.data)
      setBookings(bookingRes.data)
      setAmenities(amenityRes.data)
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!authUser) {
      return undefined
    }

    let cancelled = false

    Promise.resolve()
      .then(() => {
        if (cancelled) {
          return null
        }
        setLoading(true)
        setError('')
        return Promise.all([
          api.get('/hotels'),
          api.get('/rooms'),
          api.get('/guests'),
          api.get('/bookings'),
          api.get('/amenities'),
        ])
      })
      .then((responses) => {
        if (cancelled || !responses) {
          return
        }
        const [hotelRes, roomRes, guestRes, bookingRes, amenityRes] = responses
        setHotels(hotelRes.data)
        setRooms(roomRes.data)
        setGuests(guestRes.data)
        setBookings(bookingRes.data)
        setAmenities(amenityRes.data)
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(extractError(requestError))
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [authUser])

  useEffect(() => {
    if (
      authUser?.role !== 'ADMIN' ||
      activeTab !== 'rooms' ||
      !adminAvailabilityFilter ||
      hotelModalOpen ||
      roomModalOpen ||
      amenityModalOpen ||
      bookingModalRoomId ||
      expandedImage
    ) {
      return undefined
    }

    const resetAvailabilityByEscape = (event) => {
      if (event.key !== 'Escape') {
        return
      }

      setAdminAvailabilityFilter(null)
      setPages((current) => ({
        ...current,
        rooms: 1,
      }))
      setNotice('Проверка дат сброшена')
    }

    window.addEventListener('keydown', resetAvailabilityByEscape)
    return () => window.removeEventListener('keydown', resetAvailabilityByEscape)
  }, [
    activeTab,
    adminAvailabilityFilter,
    amenityModalOpen,
    authUser?.role,
    bookingModalRoomId,
    expandedImage,
    hotelModalOpen,
    roomModalOpen,
  ])

  const hotelMap = useMemo(
    () => new Map(hotels.map((hotel) => [hotel.id, hotel])),
    [hotels],
  )
  const roomMap = useMemo(() => new Map(rooms.map((room) => [room.id, room])), [rooms])
  const guestMap = useMemo(
    () => new Map(guests.map((guest) => [guest.id, guest])),
    [guests],
  )
  const amenityMap = useMemo(
    () => new Map(amenities.map((amenity) => [amenity.id, amenity])),
    [amenities],
  )
  const roomsByHotel = useMemo(() => {
    return rooms.reduce((result, room) => {
      const key = room.hotelId
      result[key] = result[key] ? [...result[key], room] : [room]
      return result
    }, {})
  }, [rooms])
  const bookingsByGuest = useMemo(() => {
    return bookings.reduce((result, booking) => {
      const key = booking.guestId
      result[key] = result[key] ? [...result[key], booking] : [booking]
      return result
    }, {})
  }, [bookings])

  const defaultHotelId = hotels[0] ? String(hotels[0].id) : ''
  const roomHotelId = roomForm.hotelId || defaultHotelId
  const availabilityHotelId = availabilityForm.hotelId
  const visibleTabs = authUser?.role === 'ADMIN' ? adminTabs : userTabs
  const currentGuest =
    authUser?.role === 'USER'
      ? guests.find(
          (guest) =>
            guest.id === authUser.guestId ||
            guest.email?.toLowerCase() === authUser.email?.toLowerCase(),
        )
      : null
  const currentGuestId = authUser?.guestId || currentGuest?.id || null
  const currentGuestBookings =
    authUser?.role === 'USER' && currentGuestId
      ? bookings.filter((booking) => booking.guestId === currentGuestId)
      : []
  const favoriteHotels = hotels.filter((hotel) => favoriteHotelIds.includes(hotel.id))
  const selectedSearchHotel = selectedSearchHotelId
    ? hotelMap.get(Number(selectedSearchHotelId))
    : null
  const selectedSearchHotelRooms = selectedSearchHotel
    ? roomsByHotel[selectedSearchHotel.id] || []
    : []
  const selectedSearchHotelRoomsForList = selectedSearchHotelRooms
    .filter((room) => {
      const matchesType = userRoomFilters.type ? room.type === userRoomFilters.type : true
      const matchesPrice = userRoomFilters.maxPrice
        ? Number(room.price || 0) <= Number(userRoomFilters.maxPrice)
        : true
      const matchesGuests = userRoomFilters.guests
        ? Number(room.capacity || 0) >= Number(userRoomFilters.guests)
        : true

      return matchesType && matchesPrice && matchesGuests
    })
    .sort((first, second) => {
      if (userRoomSort === 'priceDesc') {
        return Number(second.price || 0) - Number(first.price || 0)
      }

      if (userRoomSort === 'capacity') {
        return Number(second.capacity || 0) - Number(first.capacity || 0)
      }

      if (userRoomSort === 'number') {
        return compareRoomNumber(first.number, second.number)
      }

      return Number(first.price || 0) - Number(second.price || 0)
    })

  const adminHotelsForList = useMemo(() => {
    const query = normalizeSearch(adminHotelQuery)
    const list = hotels
      .filter((hotel) => matchesQuery([hotel.name, hotel.city, hotel.address], query))
      .sort((first, second) => {
        if (adminHotelSort === 'city') {
          return compareText(first.city, second.city) || compareText(first.name, second.name)
        }

        if (adminHotelSort === 'stars') {
          return Number(second.stars || 0) - Number(first.stars || 0) || compareText(first.name, second.name)
        }

        return compareText(first.name, second.name)
      })

    return list
  }, [adminHotelQuery, adminHotelSort, hotels])

  const adminRoomsForList = useMemo(() => {
    const selectedHotelId = Number(availabilityHotelId)
    const list = rooms
      .filter((room) => (selectedHotelId ? Number(room.hotelId) === selectedHotelId : true))
      .filter((room) => {
        if (!adminAvailabilityFilter) {
          return true
        }

        return (
          room.available &&
          !bookings.some(
            (booking) =>
              booking.roomId === room.id &&
              booking.status !== 'CANCELLED' &&
              adminAvailabilityFilter.checkIn <= booking.checkOutDate &&
              adminAvailabilityFilter.checkOut >= booking.checkInDate,
          )
        )
      })
      .sort((first, second) => {
        if (!selectedHotelId) {
          const firstHotel = first.hotelName || hotelMap.get(first.hotelId)?.name || ''
          const secondHotel = second.hotelName || hotelMap.get(second.hotelId)?.name || ''
          const hotelCompare = compareText(firstHotel, secondHotel)

          if (hotelCompare) {
            return hotelCompare
          }
        }

        return compareRoomNumber(first.number, second.number)
      })

    return list
  }, [adminAvailabilityFilter, availabilityHotelId, bookings, hotelMap, rooms])

  const adminBookingsForList = useMemo(() => {
    const query = normalizeSearch(adminBookingQuery)
    const list = bookings
      .filter((booking) => {
        const room = roomMap.get(booking.roomId)
        const hotel = hotelMap.get(room?.hotelId)
        const guest = guestMap.get(booking.guestId)
        const guestName = booking.guestName || `${guest?.firstName || ''} ${guest?.lastName || ''}`

        return matchesQuery(
          [
            guestName,
            guest?.email,
            booking.roomNumber,
            room?.number,
            booking.hotelName,
            room?.hotelName,
            hotel?.name,
          ],
          query,
        )
      })
      .sort((first, second) => {
        const firstRoom = roomMap.get(first.roomId)
        const secondRoom = roomMap.get(second.roomId)
        const firstHotel = first.hotelName || firstRoom?.hotelName || hotelMap.get(firstRoom?.hotelId)?.name || ''
        const secondHotel = second.hotelName || secondRoom?.hotelName || hotelMap.get(secondRoom?.hotelId)?.name || ''
        const firstGuest = guestMap.get(first.guestId)
        const secondGuest = guestMap.get(second.guestId)
        const firstGuestName = first.guestName || `${firstGuest?.firstName || ''} ${firstGuest?.lastName || ''}`
        const secondGuestName = second.guestName || `${secondGuest?.firstName || ''} ${secondGuest?.lastName || ''}`

        if (adminBookingSort === 'checkIn') {
          return compareText(first.checkInDate, second.checkInDate)
        }

        if (adminBookingSort === 'guest') {
          return compareText(firstGuestName, secondGuestName)
        }

        if (adminBookingSort === 'hotel') {
          return compareText(firstHotel, secondHotel)
        }

        if (adminBookingSort === 'status') {
          return compareText(first.status, second.status)
        }

        return getBookingSortRank(second) - getBookingSortRank(first)
      })

    return list
  }, [adminBookingQuery, adminBookingSort, bookings, guestMap, hotelMap, roomMap])

  const userBookingsForList = [...currentGuestBookings].sort(
    (first, second) => getBookingSortRank(second) - getBookingSortRank(first),
  )

  const hotelPage = getPagedItems(hotels, pages.hotels, HOTEL_PAGE_SIZE)
  const adminHotelPage = getPagedItems(adminHotelsForList, pages.hotels, HOTEL_PAGE_SIZE)
  const roomPage = getPagedItems(adminRoomsForList, pages.rooms, ADMIN_PAGE_SIZE)
  const bookingPage = getPagedItems(adminBookingsForList, pages.bookings, ADMIN_PAGE_SIZE)
  const guestPage = getPagedItems(guests, pages.guests, ADMIN_PAGE_SIZE)
  const amenityPage = getPagedItems(amenities, pages.amenities, ADMIN_PAGE_SIZE)
  const myBookingPage = getPagedItems(userBookingsForList, pages.myBookings, ADMIN_PAGE_SIZE)
  const favoritePage = getPagedItems(favoriteHotels, pages.favorites, HOTEL_PAGE_SIZE)
  const userRoomPage = getPagedItems(selectedSearchHotelRoomsForList, pages.userRooms, ADMIN_PAGE_SIZE)
  const bookingModalRoom = bookingModalRoomId ? roomMap.get(Number(bookingModalRoomId)) : null

  const onFormChange = (setter) => (event) => {
    const { checked, name, type, value } = event.target
    setter((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const onHotelFiltersChange = (event) => {
    const { name, value } = event.target

    setHotelFilters((current) => {
      const next = { ...current, [name]: value }

      if (name === 'checkInDate' && value && next.checkOutDate <= value) {
        next.checkOutDate = addDaysToDate(value, 1)
      }

      if (name === 'checkOutDate' && value && value <= next.checkInDate) {
        next.checkOutDate = addDaysToDate(next.checkInDate, 1)
      }

      return next
    })
  }

  const onAvailabilityChange = (event) => {
    const { name, value } = event.target
    setListPage('rooms', 1)
    if (name !== 'hotelId') {
      setAdminAvailabilityFilter(null)
    }

    setAvailabilityForm((current) => {
      const next = { ...current, [name]: value }

      if (name === 'checkIn' && value && next.checkOut <= value) {
        next.checkOut = addDaysToDate(value, 1)
      }

      if (name === 'checkOut' && value && value <= next.checkIn) {
        next.checkOut = addDaysToDate(next.checkIn, 1)
      }

      return next
    })
  }

  const setListPage = (key, page) => {
    setPages((current) => ({
      ...current,
      [key]: page,
    }))
  }

  const togglePassword = (key) => {
    setVisiblePasswords((current) => ({
      ...current,
      [key]: !current[key],
    }))
  }

  const toggleFavoriteHotel = (hotelId) => {
    setFavoriteHotelIds((current) => {
      const id = Number(hotelId)
      const next = current.includes(id)
        ? current.filter((favoriteId) => favoriteId !== id)
        : [...current, id]

      localStorage.setItem(FAVORITES_KEY, JSON.stringify(next))
      return next
    })
  }

  const saveBookingExtra = (bookingId, extra) => {
    setBookingExtras((current) => {
      const next = {
        ...current,
        [bookingId]: extra,
      }

      localStorage.setItem(BOOKING_EXTRAS_KEY, JSON.stringify(next))
      return next
    })
  }

  const getHotelImages = (hotel, index = 0) => {
    const images = parseImageUrls(hotel?.imageUrl)
    return images.length ? images : [defaultHotelImages[index % defaultHotelImages.length]]
  }

  const getRoomImages = (room, index = 0) => {
    const images = parseImageUrls(room?.imageUrl)
    return images.length ? images : [defaultRoomImages[index % defaultRoomImages.length]]
  }

  const openImageModal = (images, index = 0, alt = 'Фото') => {
    const preparedImages = images.filter(Boolean)

    if (!preparedImages.length) {
      return
    }

    setExpandedImage({
      alt,
      images: preparedImages,
      index: Math.min(Math.max(index, 0), preparedImages.length - 1),
    })
  }

  const shiftExpandedImage = (direction) => {
    setExpandedImage((current) => {
      if (!current?.images?.length) {
        return current
      }

      return {
        ...current,
        index: (current.index + direction + current.images.length) % current.images.length,
      }
    })
  }

  const renderZoomableImage = (src, alt, className = '', images = [src], index = 0) => (
    <button
      className={`zoomable-image ${className}`.trim()}
      type="button"
      aria-label={`Увеличить фото: ${alt}`}
      onClick={() => openImageModal(images, index, alt)}
    >
      <img src={src} alt={alt} />
    </button>
  )

  const getCarouselIndex = (imageKey, total) => {
    if (!total) {
      return 0
    }

    return Math.min(imageIndexes[imageKey] || 0, total - 1)
  }

  const shiftCarouselImage = (imageKey, total, direction) => {
    setImageIndexes((current) => {
      const currentIndex = current[imageKey] || 0
      return {
        ...current,
        [imageKey]: (currentIndex + direction + total) % total,
      }
    })
  }

  const renderImageCarousel = (images, alt, imageKey, className = '') => {
    const preparedImages = images.filter(Boolean)
    const currentIndex = getCarouselIndex(imageKey, preparedImages.length)
    const currentImage = preparedImages[currentIndex]

    if (!currentImage) {
      return null
    }

    return (
      <div className={`image-carousel ${className}`.trim()}>
        {renderZoomableImage(currentImage, alt, '', preparedImages, currentIndex)}
        {preparedImages.length > 1 && (
          <>
            <button
              className="carousel-arrow carousel-arrow-left"
              type="button"
              aria-label="Предыдущее фото"
              onClick={(event) => {
                event.stopPropagation()
                shiftCarouselImage(imageKey, preparedImages.length, -1)
              }}
            >
              ‹
            </button>
            <button
              className="carousel-arrow carousel-arrow-right"
              type="button"
              aria-label="Следующее фото"
              onClick={(event) => {
                event.stopPropagation()
                shiftCarouselImage(imageKey, preparedImages.length, 1)
              }}
            >
              ›
            </button>
            <span className="carousel-count">
              {currentIndex + 1}/{preparedImages.length}
            </span>
          </>
        )}
      </div>
    )
  }

  const renderPhotoStrip = (images, alt) => {
    if (images.length <= 1) {
      return null
    }

    return (
      <div className="photo-strip">
        {images.slice(0, 5).map((image, index) => (
          <button
            className="photo-thumb"
            type="button"
            key={`${image}-${index}`}
            aria-label={`Открыть фото ${index + 1}: ${alt}`}
            onClick={() => openImageModal(images, index, alt)}
          >
            <img src={image} alt={`${alt}, фото ${index + 1}`} />
          </button>
        ))}
      </div>
    )
  }

  const datesOverlap = (startA, endA, startB, endB) =>
    startA <= endB && endA >= startB

  const isRoomBooked = (roomId, checkIn = hotelFilters.checkInDate, checkOut = hotelFilters.checkOutDate) =>
    bookings.some(
      (booking) =>
        booking.roomId === roomId &&
        booking.status !== 'CANCELLED' &&
        datesOverlap(checkIn, checkOut, booking.checkInDate, booking.checkOutDate),
    )

  const removeImageFromForm = (setter, indexToRemove) => {
    setter((current) => {
      const images = parseImageUrls(current.imageUrl)
      const nextImages = images.filter((_, index) => index !== indexToRemove)
      return { ...current, imageUrl: serializeImageUrls(nextImages) }
    })
  }

  const renderAdminImageGallery = (form, setter, alt) => {
    const images = parseImageUrls(form.imageUrl)

    if (!images.length) {
      return null
    }

    return (
      <div className="image-preview-gallery">
        {images.map((image, index) => (
          <div className="image-preview-item" key={`${image}-${index}`}>
            {renderZoomableImage(image, `${alt} ${index + 1}`, '', images, index)}
            <button
              className="gallery-delete-button"
              type="button"
              onClick={() => removeImageFromForm(setter, index)}
            >
              Удалить фото
            </button>
          </div>
        ))}
      </div>
    )
  }

  const getAmenityNames = (room) =>
    room?.amenityIds?.map((id) => amenityMap.get(id)?.name).filter(Boolean) || []

  const normalizeAmenityIds = (ids = []) =>
    [...new Set(ids.map((id) => Number(id)).filter(Boolean))]

  const addAmenityToForm = (setter) => (event) => {
    const amenityId = Number(event.target.value)
    if (!amenityId) {
      return
    }

    setter((current) => ({
      ...current,
      amenityIds: normalizeAmenityIds([...(current.amenityIds || []), amenityId]),
    }))
  }

  const removeAmenityFromForm = (setter, amenityId) => {
    setter((current) => ({
      ...current,
      amenityIds: normalizeAmenityIds(current.amenityIds).filter((id) => id !== amenityId),
    }))
  }

  const openRoomBrowser = (hotelId) => {
    setSelectedSearchHotelId(String(hotelId))
    setUserRoomFilters({
      type: '',
      maxPrice: '',
      guests: hotelFilters.guests ? String(hotelFilters.guests) : '',
    })
    setUserRoomSort('priceAsc')
    setListPage('userRooms', 1)
    setRoomBrowserOpen(true)
  }

  const closeRoomBrowser = () => {
    setRoomBrowserOpen(false)
    setSelectedSearchHotelId('')
  }

  const syncAmenityLinks = async (target, entityId, previousIds = [], nextIds = []) => {
    const previous = normalizeAmenityIds(previousIds)
    const next = normalizeAmenityIds(nextIds)
    const toAdd = next.filter((id) => !previous.includes(id))
    const toRemove = previous.filter((id) => !next.includes(id))

    await Promise.all([
      ...toAdd.map((amenityId) => api.post(`/amenities/${amenityId}/${target}/${entityId}`)),
      ...toRemove.map((amenityId) => api.delete(`/amenities/${amenityId}/${target}/${entityId}`)),
    ])
  }

  const renderAmenityEditor = (selectedIds, setter, label) => {
    const normalizedIds = normalizeAmenityIds(selectedIds)
    const availableAmenities = amenities.filter((amenity) => !normalizedIds.includes(amenity.id))

    return (
      <div className="amenity-editor">
        <div className="amenity-editor-list">
          {normalizedIds.length ? (
            normalizedIds.map((amenityId) => {
              const amenity = amenityMap.get(amenityId)

              if (!amenity) {
                return null
              }

              return (
                <span className="amenity-chip" key={amenityId}>
                  {amenity.name}
                  <button
                    type="button"
                    aria-label={`Удалить удобство ${amenity.name}`}
                    onClick={() => removeAmenityFromForm(setter, amenityId)}
                  >
                    Удалить
                  </button>
                </span>
              )
            })
          ) : (
            <span className="muted">Удобства пока не выбраны</span>
          )}
        </div>
        <Field label={label}>
          <select value="" onChange={addAmenityToForm(setter)}>
            <option value="">Выбрать удобство</option>
            {availableAmenities.map((amenity) => (
              <option key={amenity.id} value={amenity.id}>
                {amenity.name}
              </option>
            ))}
          </select>
        </Field>
      </div>
    )
  }

  const handleImageUpload = (setter) => (event) => {
    const files = Array.from(event.target.files || [])
    if (!files.length) {
      return
    }

    if (files.some((file) => !file.type.startsWith('image/'))) {
      setError('Выберите файл изображения')
      return
    }

    Promise.all(
      files.map(
        (file) =>
          new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.onload = () => resolve(reader.result)
            reader.onerror = reject
            reader.readAsDataURL(file)
          }),
      ),
    )
      .then((newImages) => {
        setter((current) => {
          const images = parseImageUrls(current.imageUrl)
          return { ...current, imageUrl: serializeImageUrls([...images, ...newImages]) }
        })
        setNotice(newImages.length > 1 ? 'Фото добавлены' : 'Фото добавлено')
      })
      .catch(() => setError('Не удалось прочитать файл изображения'))
    event.target.value = ''
  }

  const findGuestIdByEmail = async (email) => {
    const encodedEmail = encodeURIComponent(email)
    const response = await api.get(`/guests/email/${encodedEmail}`)
    return response.data.id
  }

  const runAction = async (action, successMessage) => {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await action()
      setNotice(successMessage)
      await loadAll()
      return true
    } catch (requestError) {
      setError(extractError(requestError))
      return false
    } finally {
      setBusy(false)
    }
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    setAuthError('')

    setBusy(true)
    try {
      const response = await api.post('/auth/login', {
        email: loginForm.email.trim().toLowerCase(),
        password: loginForm.password,
      })
      const user = response.data
      saveSession(user)
      setAuthUser(user)
      setActiveTab(user.role === 'ADMIN' ? 'hotels' : 'search')
    } catch (requestError) {
      setAuthError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  const handleRegister = async (event) => {
    event.preventDefault()
    setAuthError('')

    const email = registerForm.email.trim().toLowerCase()
    if (registerForm.password.length < 6) {
      setAuthError('Пароль должен быть не короче 6 символов')
      return
    }
    if (registerForm.password !== registerForm.confirmPassword) {
      setAuthError('Пароли не совпадают')
      return
    }

    setBusy(true)
    try {
      const response = await api.post('/auth/register', {
        firstName: registerForm.firstName.trim(),
        lastName: registerForm.firstName.trim(),
        email,
        password: registerForm.password,
      })
      const user = response.data
      saveSession(user)
      setAuthUser(user)
      setActiveTab(user.role === 'ADMIN' ? 'hotels' : 'search')
    } catch (requestError) {
      setAuthError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  const logout = () => {
    localStorage.removeItem(SESSION_KEY)
    localStorage.removeItem(LEGACY_SESSION_KEY)
    localStorage.removeItem(LEGACY_USERS_KEY)
    setAuthUser(null)
    setActiveTab('search')
    setLoginForm({ email: '', password: '' })
    setSelectedSearchHotelId('')
    setPages({
      hotels: 1,
      rooms: 1,
      bookings: 1,
      guests: 1,
      amenities: 1,
      myBookings: 1,
      favorites: 1,
      userRooms: 1,
    })
  }

  const handleHotelSubmit = async (event) => {
    event.preventDefault()
    const selectedAmenityIds = normalizeAmenityIds(hotelForm.amenityIds)
    const previousAmenityIds = hotelForm.id
      ? normalizeAmenityIds(hotelMap.get(Number(hotelForm.id))?.amenityIds)
      : []
    const payload = {
      name: hotelForm.name.trim(),
      address: hotelForm.address.trim() || null,
      city: hotelForm.city.trim(),
      stars: toNumber(hotelForm.stars, 4),
      description: hotelForm.description.trim() || null,
      imageUrl: serializeImageUrls(parseImageUrls(hotelForm.imageUrl)) || null,
      pricePerNight: toNumber(hotelForm.pricePerNight, 120),
      available: hotelForm.available,
    }
    const request = async () => {
      const response = hotelForm.id
        ? await api.put(`/hotels/${hotelForm.id}`, payload)
        : await api.post('/hotels', payload)
      const hotelId = response.data?.id || hotelForm.id

      await syncAmenityLinks('hotel', hotelId, previousAmenityIds, selectedAmenityIds)
    }

    const saved = await runAction(request, hotelForm.id ? 'Отель обновлен' : 'Отель создан')
    if (saved) {
      closeHotelModal()
    }
  }

  const handleRoomSubmit = async (event) => {
    event.preventDefault()
    const selectedAmenityIds = normalizeAmenityIds(roomForm.amenityIds)
    const previousAmenityIds = roomForm.id
      ? normalizeAmenityIds(roomMap.get(Number(roomForm.id))?.amenityIds)
      : []
    const payload = {
      number: roomForm.number.trim(),
      floor: toNumber(roomForm.floor, 1),
      capacity: toNumber(roomForm.capacity, 2),
      type: roomForm.type,
      price: toNumber(roomForm.price, 120),
      imageUrl: serializeImageUrls(parseImageUrls(roomForm.imageUrl)) || null,
      available: roomForm.available,
      hotelId: toNumber(roomHotelId),
    }
    const request = async () => {
      const response = roomForm.id
        ? await api.put(`/rooms/${roomForm.id}`, payload)
        : await api.post('/rooms', payload)
      const roomId = response.data?.id || roomForm.id

      await syncAmenityLinks('room', roomId, previousAmenityIds, selectedAmenityIds)
    }

    const saved = await runAction(request, roomForm.id ? 'Номер обновлен' : 'Номер создан')
    if (saved) {
      closeRoomModal()
    }
  }

  const handleGuestSubmit = (event) => {
    event.preventDefault()
    const payload = {
      firstName: guestForm.firstName.trim(),
      lastName: guestForm.lastName.trim(),
      email: guestForm.email.trim(),
      phone: guestForm.phone.trim() || null,
    }
    const request = guestForm.id
      ? () => api.put(`/guests/${guestForm.id}`, payload)
      : () => api.post('/guests', payload)

    runAction(request, guestForm.id ? 'Гость обновлен' : 'Гость создан')
    setGuestForm(emptyGuest)
  }

  const handleAmenitySubmit = async (event) => {
    event.preventDefault()
    const payload = {
      name: amenityForm.name.trim(),
      description: amenityForm.description.trim() || null,
      icon: amenityForm.icon.trim() || 'sparkle',
    }
    const request = amenityForm.id
      ? () => api.put(`/amenities/${amenityForm.id}`, payload)
      : () => api.post('/amenities', payload)

    const saved = await runAction(request, amenityForm.id ? 'Удобство обновлено' : 'Удобство создано')
    if (saved) {
      closeAmenityModal()
    }
  }

  const openBookingModal = (room) => {
    if (!isDateRangeValid(hotelFilters.checkInDate, hotelFilters.checkOutDate)) {
      setError('Дата выезда должна быть позже даты заезда')
      return
    }

    if (isRoomBooked(room.id)) {
      setError('Этот номер уже занят на выбранные даты')
      return
    }

    const guest = currentGuest
    setBookingGuestForm({
      firstName: guest?.firstName || authUser.firstName || '',
      lastName: guest?.lastName || '',
      email: authUser.email || guest?.email || '',
      phone: guest?.phone || authUser.phone || '',
      guests: Math.min(
        Number(hotelFilters.guests) || 1,
        Number(room.capacity) || Number(hotelFilters.guests) || 1,
      ),
      paymentMethod: 'at_hotel',
    })
    setBookingModalRoomId(room.id)
  }

  const closeBookingModal = () => {
    setBookingModalRoomId(null)
  }

  const saveGuestForBooking = async () => {
    const payload = {
      firstName: bookingGuestForm.firstName.trim(),
      lastName: bookingGuestForm.lastName.trim(),
      email: authUser.email.trim().toLowerCase(),
      phone: bookingGuestForm.phone.trim() || null,
    }

    if (currentGuestId) {
      try {
        const response = await api.put(`/guests/${currentGuestId}`, payload)
        return response.data
      } catch (requestError) {
        if (requestError?.response?.status !== 404) {
          throw requestError
        }
      }
    }

    try {
      const guestId = await findGuestIdByEmail(payload.email)
      const response = await api.put(`/guests/${guestId}`, payload)
      return response.data
    } catch (requestError) {
      if (requestError?.response?.status && requestError.response.status !== 404) {
        throw requestError
      }
      const response = await api.post('/guests', payload)
      return response.data
    }
  }

  const handleBookingSubmit = async (event) => {
    event.preventDefault()

    if (!bookingModalRoom) {
      return
    }

    await runAction(async () => {
      if (!isDateRangeValid(hotelFilters.checkInDate, hotelFilters.checkOutDate)) {
        throw new Error('Дата выезда должна быть позже даты заезда')
      }

      if (isRoomBooked(bookingModalRoom.id)) {
        throw new Error('Этот номер уже занят на выбранные даты')
      }

      const guest = await saveGuestForBooking()
      const response = await api.post('/bookings', {
        checkInDate: hotelFilters.checkInDate,
        checkOutDate: hotelFilters.checkOutDate,
        roomId: bookingModalRoom.id,
        guestId: guest.id,
        status: 'PENDING',
      })

      if (response.data?.id) {
        saveBookingExtra(response.data.id, {
          guests: Number(bookingGuestForm.guests) || 1,
          paymentMethod: bookingGuestForm.paymentMethod,
        })
      }

      const updatedUser = {
        ...authUser,
        firstName: guest.firstName,
        lastName: guest.lastName,
        phone: guest.phone,
        guestId: guest.id,
      }
      saveSession(updatedUser)
      setAuthUser(updatedUser)
      closeBookingModal()
    }, 'Заявка на бронирование создана')
  }

  const filterHotels = async (event) => {
    event.preventDefault()
    if (!isDateRangeValid(hotelFilters.checkInDate, hotelFilters.checkOutDate)) {
      setError('Дата выезда должна быть позже даты заезда')
      return
    }

    setBusy(true)
    setError('')
    setNotice('')
    try {
      const params = {}
      if (hotelFilters.city.trim()) {
        params.city = hotelFilters.city.trim()
      }
      if (hotelFilters.stars) {
        params.stars = hotelFilters.stars
      }
      const response = await api.get('/hotels', { params })
      setHotels(response.data)
      setSelectedSearchHotelId('')
      setListPage('hotels', 1)
      setNotice('Фильтр применен')
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  const filterHotelsByRoom = async (event) => {
    event?.preventDefault()
    if (!isDateRangeValid(hotelFilters.checkInDate, hotelFilters.checkOutDate)) {
      setError('Дата выезда должна быть позже даты заезда')
      return
    }

    setBusy(true)
    setError('')
    setNotice('')
    try {
      const response = await api.get('/hotels/filter/native', {
        params: {
          roomType: hotelFilters.roomType,
          minPrice: toNumber(hotelFilters.minPrice, 0),
          page: 0,
          size: 20,
        },
      })
      const filteredHotels = response.data.content || response.data
      const city = hotelFilters.city.trim().toLowerCase()
      const nextHotels = filteredHotels.filter((hotel) => {
        const matchesCity = city ? hotel.city?.toLowerCase().includes(city) : true
        const matchesStars = hotelFilters.stars
          ? Number(hotel.stars) === Number(hotelFilters.stars)
          : true

        return matchesCity && matchesStars
      })

      setHotels(nextHotels)
      setSelectedSearchHotelId('')
      setListPage('hotels', 1)
      setShowAdvancedFilters(false)
      setNotice('Фильтры применены')
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  const searchAvailableRooms = (event) => {
    event.preventDefault()
    if (!isDateRangeValid(availabilityForm.checkIn, availabilityForm.checkOut)) {
      setError('Дата выезда должна быть позже даты заезда')
      return
    }

    setError('')
    setNotice('')
    setListPage('rooms', 1)
    setAdminAvailabilityFilter({
      checkIn: availabilityForm.checkIn,
      checkOut: availabilityForm.checkOut,
    })
    setNotice('Показаны только доступные номера')
  }

  const findGuestByEmail = async (event) => {
    event.preventDefault()
    if (!guestEmailQuery.trim()) {
      setListPage('guests', 1)
      loadAll()
      return
    }
    setBusy(true)
    setError('')
    setNotice('')
    try {
      const response = await api.get(`/guests/email/${encodeURIComponent(guestEmailQuery.trim())}`)
      setGuests([response.data])
      setListPage('guests', 1)
      setNotice('Гость найден')
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  const deleteEntity = (path, successMessage) => {
    runAction(() => api.delete(path), successMessage)
  }

  const updateBookingStatus = (bookingId, status) => {
    runAction(
      () => api.patch(`/bookings/${bookingId}/status`, null, { params: { status } }),
      'Статус бронирования обновлен',
    )
  }

  const cancelBooking = (bookingId) => {
    runAction(() => api.post(`/bookings/${bookingId}/cancel`), 'Бронирование отменено')
  }

  const deleteCancelledBooking = async (bookingId) => {
    const deleted = await runAction(
      () => api.delete(`/bookings/${bookingId}`),
      'Отмененное бронирование удалено',
    )

    if (deleted) {
      setBookingExtras((current) => {
        const next = { ...current }
        delete next[bookingId]
        localStorage.setItem(BOOKING_EXTRAS_KEY, JSON.stringify(next))
        return next
      })
    }
  }

  const resetFilters = () => {
    setHotelFilters({
      city: '',
      stars: '',
      roomType: 'STANDARD',
      minPrice: 80,
      checkInDate: addDays(0),
      checkOutDate: addDays(1),
      guests: 2,
    })
    setSelectedSearchHotelId('')
    setShowAdvancedFilters(false)
    setPages({
      hotels: 1,
      rooms: 1,
      bookings: 1,
      guests: 1,
      amenities: 1,
      myBookings: 1,
      favorites: 1,
      userRooms: 1,
    })
    loadAll()
  }

  const openHotelModal = (hotel = null) => {
    setHotelForm({
      ...emptyHotel,
      ...(hotel || {}),
      pricePerNight: hotel?.pricePerNight || 120,
      amenityIds: normalizeAmenityIds(hotel?.amenityIds),
    })
    setActiveTab('hotels')
    setHotelModalOpen(true)
  }

  const closeHotelModal = () => {
    setHotelModalOpen(false)
    setHotelForm(emptyHotel)
  }

  const openRoomModal = (room = null) => {
    setRoomForm({
      ...emptyRoom,
      ...(room || {}),
      hotelId: room ? String(room.hotelId) : defaultHotelId,
      amenityIds: normalizeAmenityIds(room?.amenityIds),
    })
    setActiveTab('rooms')
    setRoomModalOpen(true)
  }

  const closeRoomModal = () => {
    setRoomModalOpen(false)
    setRoomForm(emptyRoom)
  }

  const openAmenityModal = (amenity = null) => {
    setAmenityForm({ ...emptyAmenity, ...(amenity || {}) })
    setActiveTab('amenities')
    setAmenityModalOpen(true)
  }

  const closeAmenityModal = () => {
    setAmenityModalOpen(false)
    setAmenityForm(emptyAmenity)
  }

  const editHotel = (hotel) => {
    openHotelModal(hotel)
  }

  const editRoom = (room) => {
    openRoomModal(room)
  }

  const editGuest = (guest) => {
    setGuestForm({ ...emptyGuest, ...guest })
    setActiveTab('guests')
  }

  const editAmenity = (amenity) => {
    openAmenityModal(amenity)
  }

  const renderHotelForm = () => (
    <form className="entity-form modal-form" onSubmit={handleHotelSubmit}>
      <Field label="Название">
        <input
          name="name"
          required
          value={hotelForm.name}
          onChange={onFormChange(setHotelForm)}
        />
      </Field>
      <Field label="Город">
        <input
          name="city"
          required
          value={hotelForm.city}
          onChange={onFormChange(setHotelForm)}
        />
      </Field>
      <Field label="Адрес">
        <input
          name="address"
          value={hotelForm.address || ''}
          onChange={onFormChange(setHotelForm)}
        />
      </Field>
      <Field label="Звезды">
        <input
          max="5"
          min="1"
          name="stars"
          type="number"
          value={hotelForm.stars}
          onChange={onFormChange(setHotelForm)}
        />
      </Field>
      <Field label="Описание">
        <textarea
          name="description"
          value={hotelForm.description || ''}
          onChange={onFormChange(setHotelForm)}
        />
      </Field>
      <Field label="Загрузить фото">
        <input
          accept="image/*"
          multiple
          type="file"
          onChange={handleImageUpload(setHotelForm)}
        />
      </Field>
      {renderAdminImageGallery(hotelForm, setHotelForm, 'Фото отеля')}
      {renderAmenityEditor(hotelForm.amenityIds, setHotelForm, 'Добавить удобство отеля')}
      <label className="check-field">
        <input
          checked={hotelForm.available}
          name="available"
          type="checkbox"
          onChange={onFormChange(setHotelForm)}
        />
        Открыт для бронирования
      </label>
      <div className="form-actions">
        <button className="primary-button" disabled={busy} type="submit">
          {hotelForm.id ? 'Сохранить' : 'Добавить'}
        </button>
        <button className="ghost-button" type="button" onClick={closeHotelModal}>
          Отмена
        </button>
      </div>
    </form>
  )

  const renderRoomForm = () => (
    <form className="entity-form modal-form" onSubmit={handleRoomSubmit}>
      <Field label="Номер">
        <input
          name="number"
          required
          value={roomForm.number}
          onChange={onFormChange(setRoomForm)}
        />
      </Field>
      <Field label="Отель">
        <select
          name="hotelId"
          required
          value={roomHotelId}
          onChange={onFormChange(setRoomForm)}
        >
          {hotels.map((hotel) => (
            <option key={hotel.id} value={hotel.id}>
              {hotel.name}
            </option>
          ))}
        </select>
      </Field>
      <Field label="Этаж">
        <input
          min="1"
          name="floor"
          type="number"
          value={roomForm.floor}
          onChange={onFormChange(setRoomForm)}
        />
      </Field>
      <Field label="Вместимость">
        <input
          min="1"
          name="capacity"
          type="number"
          value={roomForm.capacity}
          onChange={onFormChange(setRoomForm)}
        />
      </Field>
      <Field label="Тип">
        <select name="type" value={roomForm.type} onChange={onFormChange(setRoomForm)}>
          {roomTypes.map((type) => (
            <option key={type} value={type}>
              {roomTypeLabel(type)}
            </option>
          ))}
        </select>
      </Field>
      <Field label="Цена">
        <input
          min="1"
          name="price"
          required
          type="number"
          value={roomForm.price}
          onChange={onFormChange(setRoomForm)}
        />
      </Field>
      <Field label="Загрузить фото">
        <input
          accept="image/*"
          multiple
          type="file"
          onChange={handleImageUpload(setRoomForm)}
        />
      </Field>
      {renderAdminImageGallery(roomForm, setRoomForm, 'Фото номера')}
      {renderAmenityEditor(roomForm.amenityIds, setRoomForm, 'Добавить удобство номера')}
      <label className="check-field">
        <input
          checked={roomForm.available}
          name="available"
          type="checkbox"
          onChange={onFormChange(setRoomForm)}
        />
        Открыт для бронирования
      </label>
      <div className="form-actions">
        <button className="primary-button" disabled={busy} type="submit">
          {roomForm.id ? 'Сохранить' : 'Добавить'}
        </button>
        <button className="ghost-button" type="button" onClick={closeRoomModal}>
          Отмена
        </button>
      </div>
    </form>
  )

  const renderAmenityForm = () => (
    <form className="entity-form modal-form" onSubmit={handleAmenitySubmit}>
      <Field label="Название">
        <input
          name="name"
          required
          value={amenityForm.name}
          onChange={onFormChange(setAmenityForm)}
        />
      </Field>
      <Field label="Описание">
        <input
          name="description"
          value={amenityForm.description || ''}
          onChange={onFormChange(setAmenityForm)}
        />
      </Field>
      <Field label="Иконка">
        <input
          name="icon"
          value={amenityForm.icon || ''}
          onChange={onFormChange(setAmenityForm)}
        />
      </Field>
      <div className="form-actions">
        <button className="primary-button" disabled={busy} type="submit">
          {amenityForm.id ? 'Сохранить' : 'Добавить'}
        </button>
        <button className="ghost-button" type="button" onClick={closeAmenityModal}>
          Отмена
        </button>
      </div>
    </form>
  )

  const renderSearchPanel = (compact = false) => (
    <form className={compact ? 'search-panel compact' : 'search-panel'} onSubmit={filterHotels}>
      <Field label="Город">
        <input
          name="city"
          placeholder="Город"
          value={hotelFilters.city}
          onChange={onHotelFiltersChange}
        />
      </Field>
          <Field label="Заселение">
        <input
          name="checkInDate"
          min={addDays(0)}
          type="date"
          value={hotelFilters.checkInDate}
          onChange={onHotelFiltersChange}
        />
      </Field>
      <Field label="Выселение">
        <input
          name="checkOutDate"
          min={addDaysToDate(hotelFilters.checkInDate, 1)}
          type="date"
          value={hotelFilters.checkOutDate}
          onChange={onHotelFiltersChange}
        />
      </Field>
      <Field label="Гости">
        <input
          min="1"
          name="guests"
          type="number"
          value={hotelFilters.guests}
          onChange={onHotelFiltersChange}
        />
      </Field>
      <div className="search-actions">
        <button className="primary-button" disabled={busy} type="submit">
          Найти отель
        </button>
        <button
          className={showAdvancedFilters ? 'ghost-button active-filter-button' : 'ghost-button'}
          type="button"
          onClick={() => setShowAdvancedFilters(true)}
        >
          Фильтры
        </button>
      </div>
    </form>
  )

  const renderAdvancedFilters = () => (
    <form className="filter-modal-form" onSubmit={filterHotelsByRoom}>
      <section className="filter-section">
        <div>
          <h3>Класс отеля</h3>
          <p>Выберите количество звезд</p>
        </div>
        <div className="filter-choice-grid stars-choice-grid">
          <button
            className={!hotelFilters.stars ? 'filter-choice active' : 'filter-choice'}
            type="button"
            onClick={() => setHotelFilters((current) => ({ ...current, stars: '' }))}
          >
            Любые
          </button>
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              className={Number(hotelFilters.stars) === star ? 'filter-choice active' : 'filter-choice'}
              key={star}
              type="button"
              onClick={() => setHotelFilters((current) => ({ ...current, stars: String(star) }))}
            >
              {star} ★
            </button>
          ))}
        </div>
      </section>

      <section className="filter-section">
        <div>
          <h3>Тип номера</h3>
          <p>Формат проживания</p>
        </div>
        <div className="filter-choice-grid">
          {roomTypes.map((type) => (
            <button
              className={hotelFilters.roomType === type ? 'filter-choice active' : 'filter-choice'}
              key={type}
              type="button"
              onClick={() => setHotelFilters((current) => ({ ...current, roomType: type }))}
            >
              {roomTypeLabel(type)}
            </button>
          ))}
        </div>
      </section>

      <section className="filter-section">
        <div>
          <h3>Цена</h3>
          <p>Минимальная стоимость номера за ночь</p>
        </div>
        <label className="price-filter-field">
          <span>Цена от</span>
          <input
            min="0"
            name="minPrice"
            type="number"
            value={hotelFilters.minPrice}
            onChange={onFormChange(setHotelFilters)}
          />
        </label>
      </section>

      <div className="filter-modal-actions">
        <button className="ghost-button" disabled={busy} type="button" onClick={resetFilters}>
          Сбросить
        </button>
        <button className="primary-button" disabled={busy} type="submit">
          Показать отели
        </button>
      </div>
    </form>
  )

  const renderRoomOffer = (room, index = 0, mode = 'user') => {
    const amenityNames = getAmenityNames(room)
    const hotelName = room.hotelName || hotelMap.get(room.hotelId)?.name || 'Отель'
    const roomImages = getRoomImages(room, index)
    const roomBooked =
      mode === 'admin'
        ? isRoomBooked(room.id, availabilityForm.checkIn, availabilityForm.checkOut)
        : isRoomBooked(room.id)
    const canBookRoom = room.available && !roomBooked
    const roomStatusLabel = roomBooked ? 'Занят на выбранные даты' : room.available ? 'Доступен' : 'Недоступен'
    const roomStatusClass = canBookRoom ? 'room-status-success' : 'room-status-danger'

    return (
      <article className="room-offer-card" key={room.id}>
        <div className="room-offer-media">
          {renderImageCarousel(roomImages, `Номер ${room.number}`, `room-${mode}-${room.id}`)}
        </div>
        <div className="room-offer-main">
          <div>
            <p className="eyebrow">{hotelName}</p>
            <h3>Номер {room.number} · {roomTypeLabel(room.type)}</h3>
          </div>
          {renderPhotoStrip(roomImages, `Номер ${room.number}`)}
          <div className="room-facts">
            <span>{guestCountLabel(room.capacity || 1)}</span>
            <span>{room.floor || 1} этаж</span>
            <span className={roomStatusClass}>{roomStatusLabel}</span>
          </div>
          {amenityNames.length > 0 && (
            <div className="pill-list">
              {amenityNames.slice(0, 4).map((name) => (
                <span className="pill" key={name}>
                  {name}
                </span>
              ))}
            </div>
          )}
        </div>
        <div className="room-offer-side">
          <span className="muted">за ночь</span>
          <strong>{formatPrice(room.price)}</strong>
          {mode === 'user' ? (
            <button
              className="primary-button"
              disabled={!canBookRoom || busy}
              type="button"
              onClick={() => openBookingModal(room)}
            >
              {roomBooked ? 'Занят' : 'Забронировать'}
            </button>
          ) : (
            <div className="card-actions">
              <button type="button" onClick={() => editRoom(room)}>
                Изменить
              </button>
              <button
                className="danger-button"
                type="button"
                onClick={() => deleteEntity(`/rooms/${room.id}`, 'Номер удален')}
              >
                Удалить
              </button>
            </div>
          )}
        </div>
      </article>
    )
  }

  const renderUserHotelResults = ({
    pageData = hotelPage,
    totalItems = hotels.length,
    pageKey = 'hotels',
    emptyTitle = 'Отели не найдены',
  } = {}) => (
    <>
      <div className="cards-grid">
        {totalItems ? (
          pageData.items.map((hotel, index) => {
            const hotelRooms = roomsByHotel[hotel.id] || []
            const hotelAmenities =
              hotel.amenityIds?.map((id) => amenityMap.get(id)).filter(Boolean) || []
            const hotelImages = getHotelImages(hotel, index)
            const minRoomPrice = hotelRooms.length
              ? Math.min(...hotelRooms.map((room) => Number(room.price || 0)))
              : null

            return (
              <article
                className={
                  selectedSearchHotelId === String(hotel.id)
                    ? 'hotel-card selected'
                    : 'hotel-card'
                }
                key={hotel.id}
              >
                <div className="card-media">
                  {renderImageCarousel(hotelImages, hotel.name, `hotel-${pageKey}-${hotel.id}`)}
                  <span className="star-badge">
                    <Stars value={hotel.stars} />
                  </span>
                  <button
                    className={
                      favoriteHotelIds.includes(hotel.id)
                        ? 'favorite-button active'
                        : 'favorite-button'
                    }
                    type="button"
                    aria-label={
                      favoriteHotelIds.includes(hotel.id)
                        ? 'Убрать из избранного'
                        : 'Добавить в избранное'
                    }
                    title={
                      favoriteHotelIds.includes(hotel.id)
                        ? 'Убрать из избранного'
                        : 'Добавить в избранное'
                    }
                    onClick={() => toggleFavoriteHotel(hotel.id)}
                  >
                    ★
                  </button>
                </div>
                <div className="card-body">
                  <div className="card-title">
                    <div>
                      <h3>{hotel.name}</h3>
                      <p>{hotel.city || 'Город не указан'}</p>
                      <p className="card-address">{hotel.address || 'Адрес не указан'}</p>
                    </div>
                    <StatusPill active={hotel.available}>
                      {hotel.available ? 'Открыт' : 'Закрыт'}
                    </StatusPill>
                  </div>
                  <p className="muted clamp-text">{hotel.description || 'Описание не задано'}</p>
                  {renderPhotoStrip(hotelImages, hotel.name)}
                  {hotelAmenities.length > 0 && (
                    <div className="pill-list">
                      {hotelAmenities.slice(0, 5).map((amenity) => (
                        <span className="pill" key={amenity.id}>
                          {amenity.name}
                        </span>
                      ))}
                    </div>
                  )}
                  <div className="booking-line">
                    <strong>
                      {minRoomPrice ? `от ${formatPrice(minRoomPrice)}` : 'Номера скоро'}
                    </strong>
                    <button
                      className="primary-button"
                      type="button"
                      onClick={() => openRoomBrowser(hotel.id)}
                    >
                      Смотреть номера
                    </button>
                  </div>
                </div>
              </article>
            )
          })
        ) : (
          <EmptyState title={emptyTitle} />
        )}
      </div>
      <Pagination
        page={pageData.page}
        pageSize={HOTEL_PAGE_SIZE}
        totalItems={totalItems}
        onPageChange={(page) => setListPage(pageKey, page)}
      />
    </>
  )

  const renderHotelCards = (withBooking = false, items = hotels) => (
    <div className="cards-grid">
      {items.length ? (
        items.map((hotel, index) => {
          const hotelRooms = roomsByHotel[hotel.id] || []
          const hotelAmenities =
            hotel.amenityIds?.map((id) => amenityMap.get(id)).filter(Boolean) || []
          const firstRoom = hotelRooms[0]
          const hotelImages = getHotelImages(hotel, index)
          return (
            <article className="hotel-card" key={hotel.id}>
              <div className="card-media">
                {renderImageCarousel(hotelImages, hotel.name, `admin-hotel-${hotel.id}`)}
                <span className="star-badge">
                  <Stars value={hotel.stars} />
                </span>
              </div>
              <div className="card-body">
                <div className="card-title">
                  <div>
                    <h3>{hotel.name}</h3>
                    <p>{hotel.city || 'Город не указан'}</p>
                    <p className="card-address">{hotel.address || 'Адрес не указан'}</p>
                  </div>
                  <StatusPill active={hotel.available}>
                    {hotel.available ? 'Открыт' : 'Закрыт'}
                  </StatusPill>
                </div>
                <p className="muted clamp-text">{hotel.description || 'Описание не задано'}</p>
                {renderPhotoStrip(hotelImages, hotel.name)}
                <div className="mini-block">
                  <strong>Номера</strong>
                  <span>{hotelRooms.map((room) => `№ ${room.number}`).join(', ') || 'нет'}</span>
                </div>
                <div className="pill-list">
                  {hotelAmenities.slice(0, 5).map((amenity) => (
                    <span className="pill" key={amenity.id}>
                      {amenity.name}
                    </span>
                  ))}
                </div>
                {withBooking ? (
                  <div className="booking-line">
                    <strong>{firstRoom ? formatPrice(firstRoom.price) : 'Нет номеров'}</strong>
                    <button
                      className="primary-button"
                      disabled={!firstRoom || busy}
                      type="button"
                      onClick={() => openBookingModal(firstRoom)}
                    >
                      Забронировать
                    </button>
                  </div>
                ) : (
                  <div className="card-actions">
                    <button type="button" onClick={() => editHotel(hotel)}>
                      Изменить
                    </button>
                    <button
                      className="danger-button"
                      type="button"
                      onClick={() => deleteEntity(`/hotels/${hotel.id}`, 'Отель удален')}
                    >
                      Удалить
                    </button>
                  </div>
                )}
              </div>
            </article>
          )
        })
      ) : (
        <EmptyState title="Отели не найдены" />
      )}
    </div>
  )

  const renderUserBookingCard = (booking, index = 0) => {
    const room = roomMap.get(booking.roomId)
    const roomImages = getRoomImages(room, index)
    const extra = bookingExtras[booking.id] || {}
    const hotelName = room?.hotelName || booking.hotelName || 'Отель'
    const bookingDates = getDisplayBookingDates(booking)
    const inactiveBooking = isBookingInactive(booking)
    const guestsCount = extra.guests || room?.capacity || hotelFilters.guests || 1
    const paymentText =
      extra.paymentMethod === 'online_hold'
        ? 'Оплата картой при подтверждении'
        : 'Оплата при заселении'

    return (
      <article className={inactiveBooking ? 'booking-room-card inactive' : 'booking-room-card'} key={booking.id}>
        <div className="booking-room-media">
          {renderImageCarousel(
            roomImages,
            `Номер ${booking.roomNumber || room?.number || ''}`,
            `booking-room-${booking.id}`,
          )}
        </div>
        <div className="booking-room-main">
          <div>
            <p className="eyebrow">{hotelName}</p>
            <h3>Номер {booking.roomNumber || room?.number || 'не указан'}</h3>
            <p className="muted">
              {roomTypeLabel(room?.type)} · {guestCountLabel(room?.capacity || extra.guests || 1)} ·{' '}
              {room?.floor || 1} этаж
            </p>
          </div>
          {renderPhotoStrip(roomImages, `Номер ${booking.roomNumber || room?.number || ''}`)}
          <div className="room-facts">
            <span>Заселение: {bookingDates.checkInDate}</span>
            <span>Выселение: {bookingDates.checkOutDate}</span>
            <span>{guestCountLabel(guestsCount)}</span>
          </div>
          <div className="pill-list">
            <StatusPill tone={bookingStatusTone(booking.status)}>
              {bookingStatusLabel(booking.status)}
            </StatusPill>
            {inactiveBooking && <span className="pill pill-danger">Недействительна</span>}
            <span className="pill">{paymentText}</span>
          </div>
        </div>
        <div className="booking-room-side">
          <span className="muted">итого</span>
          <strong>{formatPrice(getBookingDisplayTotal(booking))}</strong>
          <button
            className="danger-button"
            disabled={booking.status === 'CANCELLED' || busy}
            type="button"
            onClick={() => cancelBooking(booking.id)}
          >
            Отменить
          </button>
        </div>
      </article>
    )
  }

  if (!authUser) {
    return (
      <main className="auth-shell">
        <Notification
          type="error"
          message={authError}
          onClose={() => setAuthError('')}
        />
        <header className="auth-topbar">
          <a className="brand" href="#top" aria-label="RoyalStay">
            <span className="brand-mark">R</span>
            <span>
              <strong>RoyalStay</strong>
              <small>Отели и апартаменты</small>
            </span>
          </a>
          <nav className="auth-links" aria-label="Навигация">
            <span>Отели</span>
            <span>Авиабилеты</span>
            <span>Поддержка</span>
          </nav>
        </header>

        <section className="auth-hero" id="top">
          <img src={hotelPhoto} alt="Отель RoyalStay" />
          <div className="auth-hero-copy">
            <p className="eyebrow">Жильё для ваших путешествий</p>
            <h1>Найдите идеальное место</h1>
          </div>

          <section className="auth-card" aria-label="Вход и регистрация">
            <div className="mode-switch">
              <button
                className={authMode === 'login' ? 'active' : ''}
                type="button"
                onClick={() => setAuthMode('login')}
              >
                Вход
              </button>
              <button
                className={authMode === 'register' ? 'active' : ''}
                type="button"
                onClick={() => setAuthMode('register')}
              >
                Регистрация
              </button>
            </div>

            {authMode === 'login' ? (
              <form className="auth-form" onSubmit={handleLogin}>
                <Field label="Почта">
                  <input
                    name="email"
                    required
                    type="email"
                    value={loginForm.email}
                    onChange={onFormChange(setLoginForm)}
                  />
                </Field>
                <Field label="Пароль">
                  <PasswordInput
                    name="password"
                    placeholder="Введите пароль"
                    required
                    value={loginForm.password}
                    visible={visiblePasswords.login}
                    onChange={onFormChange(setLoginForm)}
                    onToggle={() => togglePassword('login')}
                  />
                </Field>
                <button className="primary-button wide" disabled={busy} type="submit">
                  Войти
                </button>
              </form>
            ) : (
              <form className="auth-form register" onSubmit={handleRegister}>
                <Field label="Имя">
                  <input
                    name="firstName"
                    required
                    value={registerForm.firstName}
                    onChange={onFormChange(setRegisterForm)}
                  />
                </Field>
                <Field label="Почта">
                  <input
                    name="email"
                    required
                    type="email"
                    value={registerForm.email}
                    onChange={onFormChange(setRegisterForm)}
                  />
                </Field>
                <Field label="Пароль">
                  <PasswordInput
                    name="password"
                    required
                    value={registerForm.password}
                    visible={visiblePasswords.register}
                    onChange={onFormChange(setRegisterForm)}
                    onToggle={() => togglePassword('register')}
                  />
                </Field>
                <Field label="Повтор пароля">
                  <PasswordInput
                    name="confirmPassword"
                    required
                    value={registerForm.confirmPassword}
                    visible={visiblePasswords.confirm}
                    onChange={onFormChange(setRegisterForm)}
                    onToggle={() => togglePassword('confirm')}
                  />
                </Field>
                <button className="primary-button wide" disabled={busy} type="submit">
                  Создать аккаунт
                </button>
              </form>
            )}

          </section>
        </section>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <Notification
        type={error ? 'error' : 'success'}
        message={error || notice}
        onClose={() => {
          setError('')
          setNotice('')
        }}
      />
      <header className="topbar">
        <a className="brand" href="#top" aria-label="RoyalStay">
          <span className="brand-mark">R</span>
          <span>
            <strong>RoyalStay</strong>
            <small>{authUser.role === 'ADMIN' ? 'Отели и апартаменты' : 'Отели и апартаменты'}</small>
          </span>
        </a>
        <nav className="tabs" aria-label="Разделы RoyalStay">
          {visibleTabs.map((tab) => (
            <button
              key={tab.id}
              className={activeTab === tab.id ? 'tab active' : 'tab'}
              type="button"
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>
        <div className="account-area">
          <span className="account-chip">
            {authUser.role === 'ADMIN' ? 'Админ' : 'Пользователь'} · {authUser.firstName}
          </span>
          <button className="logout-button" type="button" onClick={logout}>
            Выйти
          </button>
        </div>
      </header>

      {authUser.role === 'USER' && activeTab === 'search' && (
        <section className="trip-hero" id="top">
          <img src={hotelPhoto} alt="Отели RoyalStay" />
          <div className="trip-hero-copy">
            <p className="eyebrow">Жильё для ваших путешествий</p>
            <h1>Найдите идеальное место</h1>
          </div>
          {renderSearchPanel()}
        </section>
      )}

      {loading && (
        <section className="status-line" aria-live="polite">
          <span>Загрузка данных...</span>
        </section>
      )}

      {authUser.role === 'USER' && activeTab === 'search' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Результаты поиска</h2>
            </div>
          </div>
          {renderUserHotelResults()}
        </section>
      )}

      {authUser.role === 'USER' && activeTab === 'favorites' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Избранные отели</h2>
            </div>
          </div>
          {renderUserHotelResults({
            pageData: favoritePage,
            totalItems: favoriteHotels.length,
            pageKey: 'favorites',
            emptyTitle: 'Добавьте отель в избранное звездочкой на карточке',
          })}
        </section>
      )}

      {authUser.role === 'USER' && activeTab === 'myBookings' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Мои бронирования</h2>
            </div>
          </div>
          <div className="booking-room-list">
            {userBookingsForList.length ? (
              myBookingPage.items.map((booking, index) => renderUserBookingCard(booking, index))
            ) : (
              <EmptyState title="У вас пока нет бронирований" />
            )}
          </div>
          <Pagination
            page={myBookingPage.page}
            pageSize={ADMIN_PAGE_SIZE}
            totalItems={userBookingsForList.length}
            onPageChange={(page) => setListPage('myBookings', page)}
          />
        </section>
      )}

      {authUser.role === 'ADMIN' && activeTab === 'hotels' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Список отелей</h2>
            </div>
            <button className="primary-button" type="button" onClick={() => openHotelModal()}>
              Добавить
            </button>
          </div>

          <div className="list-toolbar">
            <Field label="Поиск отеля">
              <input
                placeholder="Название, город или адрес"
                value={adminHotelQuery}
                onChange={(event) => {
                  setAdminHotelQuery(event.target.value)
                  setListPage('hotels', 1)
                }}
              />
            </Field>
            <Field label="Сортировка">
              <select
                value={adminHotelSort}
                onChange={(event) => {
                  setAdminHotelSort(event.target.value)
                  setListPage('hotels', 1)
                }}
              >
                <option value="name">По названию</option>
                <option value="city">По городу</option>
                <option value="stars">Сначала больше звезд</option>
              </select>
            </Field>
          </div>

          {renderHotelCards(false, adminHotelPage.items)}
          <Pagination
            page={adminHotelPage.page}
            pageSize={HOTEL_PAGE_SIZE}
            totalItems={adminHotelsForList.length}
            onPageChange={(page) => setListPage('hotels', page)}
          />
        </section>
      )}

      {authUser.role === 'ADMIN' && activeTab === 'rooms' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Список номеров</h2>
            </div>
            <button className="primary-button" type="button" onClick={() => openRoomModal()}>
              Добавить
            </button>
          </div>

          <form className="availability-toolbar" onSubmit={searchAvailableRooms}>
            <Field label="Отель">
              <select
                name="hotelId"
                value={availabilityHotelId}
                onChange={onAvailabilityChange}
              >
                <option value="">Все отели</option>
                {hotels.map((hotel) => (
                  <option key={hotel.id} value={hotel.id}>
                    {hotel.name}
                  </option>
                ))}
              </select>
            </Field>
            <div className="availability-actions">
              <Field label="Заселение">
                <input
                  name="checkIn"
                  min={addDays(0)}
                  required
                  type="date"
                  value={availabilityForm.checkIn}
                  onChange={onAvailabilityChange}
                />
              </Field>
              <Field label="Выселение">
                <input
                  name="checkOut"
                  min={addDaysToDate(availabilityForm.checkIn, 1)}
                  required
                  type="date"
                  value={availabilityForm.checkOut}
                  onChange={onAvailabilityChange}
                />
              </Field>
              <button className="primary-button" disabled={busy} type="submit">
                Проверить даты
              </button>
            </div>
          </form>

          <div className="room-offer-list">
            {adminRoomsForList.length ? (
              roomPage.items.map((room, index) => renderRoomOffer(room, index, 'admin'))
            ) : (
              <EmptyState title="Номера не найдены" />
            )}
          </div>
          <Pagination
            page={roomPage.page}
            pageSize={ADMIN_PAGE_SIZE}
            totalItems={adminRoomsForList.length}
            onPageChange={(page) => setListPage('rooms', page)}
          />
        </section>
      )}

      {authUser.role === 'ADMIN' && activeTab === 'bookings' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Бронирования</h2>
            </div>
          </div>

          <div className="list-toolbar">
            <Field label="Поиск брони">
              <input
                placeholder="Гость, отель или номер"
                value={adminBookingQuery}
                onChange={(event) => {
                  setAdminBookingQuery(event.target.value)
                  setListPage('bookings', 1)
                }}
              />
            </Field>
            <Field label="Сортировка">
              <select
                value={adminBookingSort}
                onChange={(event) => {
                  setAdminBookingSort(event.target.value)
                  setListPage('bookings', 1)
                }}
              >
                <option value="newest">Сначала новые</option>
                <option value="checkIn">По заселению</option>
                <option value="guest">По гостю</option>
                <option value="hotel">По отелю</option>
                <option value="status">По статусу</option>
              </select>
            </Field>
          </div>

          <div className="table-list">
            {adminBookingsForList.length ? (
              bookingPage.items.map((booking) => {
                const bookingDates = getDisplayBookingDates(booking)
                const room = roomMap.get(booking.roomId)
                const hotel = hotelMap.get(room?.hotelId)
                const guest = guestMap.get(booking.guestId)
                const guestName =
                  booking.guestName ||
                  `${guest?.firstName || ''} ${guest?.lastName || ''}`.trim() ||
                  'Гость не указан'
                const hotelName = booking.hotelName || room?.hotelName || hotel?.name || 'Отель не указан'

                return (
                  <article className="table-card booking-card" key={booking.id}>
                    <div>
                      <strong>{guestName}</strong>
                      <span>{guest?.email || 'почта не указана'}</span>
                    </div>
                    <div>
                      <strong>{hotelName}</strong>
                      <span>№ {booking.roomNumber || room?.number}</span>
                    </div>
                    <div>
                      <span>{bookingDates.checkInDate}</span>
                      <span>{bookingDates.checkOutDate}</span>
                    </div>
                    <div>
                      <strong>{formatPrice(getBookingDisplayTotal(booking))}</strong>
                      <StatusPill tone={bookingStatusTone(booking.status)}>
                        {bookingStatusLabel(booking.status)}
                      </StatusPill>
                    </div>
                    <div className="card-actions">
                      <select
                        value={booking.status}
                        onChange={(event) => updateBookingStatus(booking.id, event.target.value)}
                      >
                        {bookingStatuses.map((status) => (
                          <option key={status} value={status}>
                            {bookingStatusLabel(status)}
                          </option>
                        ))}
                      </select>
                      <button
                        className="icon-danger-button"
                        disabled={booking.status !== 'CANCELLED' || busy}
                        type="button"
                        aria-label="Удалить отмененное бронирование"
                        title="Удалить отмененное бронирование"
                        onClick={() => deleteCancelledBooking(booking.id)}
                      >
                        ×
                      </button>
                    </div>
                  </article>
                )
              })
            ) : (
              <EmptyState title="Бронирования не найдены" />
            )}
          </div>
          <Pagination
            page={bookingPage.page}
            pageSize={ADMIN_PAGE_SIZE}
            totalItems={adminBookingsForList.length}
            onPageChange={(page) => setListPage('bookings', page)}
          />
        </section>
      )}

      {authUser.role === 'ADMIN' && activeTab === 'guests' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Список клиентов</h2>
            </div>
            <form className="inline-search" onSubmit={findGuestByEmail}>
              <input
                placeholder="Почта клиента"
                type="email"
                value={guestEmailQuery}
                onChange={(event) => setGuestEmailQuery(event.target.value)}
              />
              <button className="ghost-button" disabled={busy} type="submit">
                Найти
              </button>
            </form>
          </div>

          {guestForm.id && (
            <form className="entity-form compact" onSubmit={handleGuestSubmit}>
              <Field label="Имя">
                <input
                  name="firstName"
                  required
                  value={guestForm.firstName}
                  onChange={onFormChange(setGuestForm)}
                />
              </Field>
              <Field label="Фамилия">
                <input
                  name="lastName"
                  required
                  value={guestForm.lastName}
                  onChange={onFormChange(setGuestForm)}
                />
              </Field>
              <Field label="Почта">
                <input
                  name="email"
                  required
                  type="email"
                  value={guestForm.email}
                  onChange={onFormChange(setGuestForm)}
                />
              </Field>
              <Field label="Телефон">
                <input
                  name="phone"
                  value={guestForm.phone || ''}
                  onChange={onFormChange(setGuestForm)}
                />
              </Field>
              <div className="form-actions">
                <button className="primary-button" disabled={busy} type="submit">
                  Сохранить
                </button>
                <button className="ghost-button" type="button" onClick={() => setGuestForm(emptyGuest)}>
                  Отмена
                </button>
              </div>
            </form>
          )}

          <div className="cards-grid slim">
            {guests.length ? (
              guestPage.items.map((guest) => (
                <article className="person-card" key={guest.id}>
                  {(() => {
                    const currentBookings = (bookingsByGuest[guest.id] || []).filter(
                      (booking) => !isBookingInactive(booking),
                    )

                    return (
                      <>
                  <h3>
                    {guest.firstName} {guest.lastName}
                  </h3>
                  <p>{guest.email}</p>
                  <p className="muted">{guest.phone || 'Телефон не указан'}</p>
                  <div className="mini-block">
                    <strong>Текущие бронирования</strong>
                    <span>
                      {currentBookings
                        .map((booking) => `#${booking.id}`)
                        .join(', ') || 'нет'}
                    </span>
                  </div>
                      </>
                    )
                  })()}
                  <div className="card-actions">
                    <button type="button" onClick={() => editGuest(guest)}>
                      Изменить
                    </button>
                    <button
                      className="danger-button"
                      type="button"
                      onClick={() => deleteEntity(`/guests/${guest.id}`, 'Гость удален')}
                    >
                      Удалить
                    </button>
                  </div>
                </article>
              ))
            ) : (
              <EmptyState title="Гости не найдены" />
            )}
          </div>
          <Pagination
            page={guestPage.page}
            pageSize={ADMIN_PAGE_SIZE}
            totalItems={guests.length}
            onPageChange={(page) => setListPage('guests', page)}
          />
        </section>
      )}

      {authUser.role === 'ADMIN' && activeTab === 'amenities' && (
        <section className="content-section">
          <div className="section-heading">
            <div>
              <h2>Список удобств</h2>
            </div>
            <button className="primary-button" type="button" onClick={() => openAmenityModal()}>
              Добавить удобство
            </button>
          </div>

          <div className="cards-grid slim">
            {amenities.length ? (
              amenityPage.items.map((amenity) => (
                <article className="person-card" key={amenity.id}>
                  <span className="amenity-icon">{amenity.icon || 'amenity'}</span>
                  <h3>{amenity.name}</h3>
                  <p className="muted">{amenity.description || 'Описание не задано'}</p>
                  <div className="card-actions">
                    <button type="button" onClick={() => editAmenity(amenity)}>
                      Изменить
                    </button>
                    <button
                      className="danger-button"
                      type="button"
                      onClick={() =>
                        deleteEntity(`/amenities/${amenity.id}`, 'Удобство удалено')
                      }
                    >
                      Удалить
                    </button>
                  </div>
                </article>
              ))
            ) : (
              <EmptyState title="Удобства не найдены" />
            )}
          </div>
          <Pagination
            page={amenityPage.page}
            pageSize={ADMIN_PAGE_SIZE}
            totalItems={amenities.length}
            onPageChange={(page) => setListPage('amenities', page)}
          />
        </section>
      )}

      {authUser.role === 'USER' && showAdvancedFilters && (
        <div
          className="filter-modal-backdrop"
          role="presentation"
          onClick={() => setShowAdvancedFilters(false)}
        >
          <aside
            className="filter-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="filters-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="filter-modal-heading">
              <div>
                <p className="eyebrow">Подбор отеля</p>
                <h2 id="filters-title">Фильтры</h2>
              </div>
              <button className="ghost-button" type="button" onClick={() => setShowAdvancedFilters(false)}>
                Закрыть
              </button>
            </div>
            {renderAdvancedFilters()}
          </aside>
        </div>
      )}

      {authUser.role === 'USER' && roomBrowserOpen && selectedSearchHotel && (
        <div className="modal-backdrop" role="presentation">
          <section className="rooms-browser-modal" role="dialog" aria-modal="true" aria-labelledby="rooms-browser-title">
            <div className="booking-modal-heading">
              <div>
                <p className="eyebrow">{selectedSearchHotel.name}</p>
                <h2 id="rooms-browser-title">Номера</h2>
              </div>
              <button className="ghost-button" type="button" onClick={closeRoomBrowser}>
                Закрыть
              </button>
            </div>

            <div className="rooms-browser-filters">
              <Field label="Тип">
                <select
                  value={userRoomFilters.type}
                  onChange={(event) =>
                    {
                      setUserRoomFilters((current) => ({ ...current, type: event.target.value }))
                      setListPage('userRooms', 1)
                    }
                  }
                >
                  <option value="">Любой</option>
                  {roomTypes.map((type) => (
                    <option key={type} value={type}>
                      {roomTypeLabel(type)}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Цена до">
                <input
                  min="0"
                  type="number"
                  value={userRoomFilters.maxPrice}
                  onChange={(event) =>
                    {
                      setUserRoomFilters((current) => ({ ...current, maxPrice: event.target.value }))
                      setListPage('userRooms', 1)
                    }
                  }
                />
              </Field>
              <Field label="Гостей">
                <input
                  min="1"
                  type="number"
                  value={userRoomFilters.guests}
                  onChange={(event) =>
                    {
                      setUserRoomFilters((current) => ({ ...current, guests: event.target.value }))
                      setListPage('userRooms', 1)
                    }
                  }
                />
              </Field>
              <Field label="Сортировка">
                <select
                  value={userRoomSort}
                  onChange={(event) => {
                    setUserRoomSort(event.target.value)
                    setListPage('userRooms', 1)
                  }}
                >
                  <option value="priceAsc">Сначала дешевле</option>
                  <option value="priceDesc">Сначала дороже</option>
                  <option value="capacity">По вместимости</option>
                  <option value="number">По номеру</option>
                </select>
              </Field>
              <button
                className="ghost-button"
                type="button"
                onClick={() => {
                  setUserRoomFilters({ type: '', maxPrice: '', guests: '' })
                  setListPage('userRooms', 1)
                }}
              >
                Сбросить
              </button>
            </div>

            <div className="room-offer-list rooms-browser-list">
              {selectedSearchHotelRoomsForList.length ? (
                userRoomPage.items.map((room, roomIndex) =>
                  renderRoomOffer(room, roomIndex, 'user'),
                )
              ) : (
                <EmptyState title="Подходящие номера не найдены" />
              )}
            </div>
            <Pagination
              page={userRoomPage.page}
              pageSize={ADMIN_PAGE_SIZE}
              totalItems={selectedSearchHotelRoomsForList.length}
              onPageChange={(page) => setListPage('userRooms', page)}
            />
          </section>
        </div>
      )}

      {authUser.role === 'ADMIN' && hotelModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="entity-modal" role="dialog" aria-modal="true" aria-labelledby="hotel-modal-title">
            <div className="booking-modal-heading">
              <div>
                <h2 id="hotel-modal-title">{hotelForm.id ? 'Изменить отель' : 'Добавить отель'}</h2>
              </div>
              <button className="ghost-button" type="button" onClick={closeHotelModal}>
                Закрыть
              </button>
            </div>
            {renderHotelForm()}
          </section>
        </div>
      )}

      {authUser.role === 'ADMIN' && roomModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="entity-modal" role="dialog" aria-modal="true" aria-labelledby="room-modal-title">
            <div className="booking-modal-heading">
              <div>
                <h2 id="room-modal-title">{roomForm.id ? 'Изменить номер' : 'Добавить номер'}</h2>
              </div>
              <button className="ghost-button" type="button" onClick={closeRoomModal}>
                Закрыть
              </button>
            </div>
            {renderRoomForm()}
          </section>
        </div>
      )}

      {authUser.role === 'ADMIN' && amenityModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="entity-modal" role="dialog" aria-modal="true" aria-labelledby="amenity-modal-title">
            <div className="booking-modal-heading">
              <div>
                <h2 id="amenity-modal-title">
                  {amenityForm.id ? 'Изменить удобство' : 'Добавить удобство'}
                </h2>
              </div>
              <button className="ghost-button" type="button" onClick={closeAmenityModal}>
                Закрыть
              </button>
            </div>
            {renderAmenityForm()}
          </section>
        </div>
      )}

      {expandedImage && (
        <div className="image-modal-backdrop" role="presentation" onClick={() => setExpandedImage(null)}>
          <button
            className="image-modal"
            type="button"
            aria-label="Закрыть увеличенное фото"
            onClick={() => setExpandedImage(null)}
          >
            <img
              src={expandedImage.images[expandedImage.index]}
              alt={`${expandedImage.alt}, фото ${expandedImage.index + 1}`}
            />
          </button>
          {expandedImage.images.length > 1 && (
            <>
              <button
                className="image-modal-arrow image-modal-arrow-left"
                type="button"
                aria-label="Предыдущее фото"
                onClick={(event) => {
                  event.stopPropagation()
                  shiftExpandedImage(-1)
                }}
              >
                ‹
              </button>
              <button
                className="image-modal-arrow image-modal-arrow-right"
                type="button"
                aria-label="Следующее фото"
                onClick={(event) => {
                  event.stopPropagation()
                  shiftExpandedImage(1)
                }}
              >
                ›
              </button>
              <span className="image-modal-count">
                {expandedImage.index + 1}/{expandedImage.images.length}
              </span>
            </>
          )}
        </div>
      )}

      {bookingModalRoom && (
        <div className="modal-backdrop" role="presentation">
          <section className="booking-modal" role="dialog" aria-modal="true" aria-labelledby="booking-title">
            <div className="booking-modal-heading">
              <div>
                <p className="eyebrow">{bookingModalRoom.hotelName || 'Бронирование'}</p>
                <h2 id="booking-title">Данные гостя</h2>
              </div>
              <button className="ghost-button" type="button" onClick={closeBookingModal}>
                Закрыть
              </button>
            </div>

            <form className="booking-modal-grid" onSubmit={handleBookingSubmit}>
              <div className="booking-guest-fields">
                <Field label="Имя">
                  <input
                    name="firstName"
                    required
                    value={bookingGuestForm.firstName}
                    onChange={onFormChange(setBookingGuestForm)}
                  />
                </Field>
                <Field label="Фамилия">
                  <input
                    name="lastName"
                    required
                    value={bookingGuestForm.lastName}
                    onChange={onFormChange(setBookingGuestForm)}
                  />
                </Field>
                <Field label="Почта аккаунта">
                  <input name="email" readOnly type="email" value={authUser.email} />
                </Field>
                <Field label="Телефон">
                  <input
                    name="phone"
                    placeholder="+375-29-123-45-67"
                    value={bookingGuestForm.phone}
                    onChange={onFormChange(setBookingGuestForm)}
                  />
                </Field>
                <Field label="Гостей">
                  <input
                    max={bookingModalRoom.capacity || 6}
                    min="1"
                    name="guests"
                    required
                    type="number"
                    value={bookingGuestForm.guests}
                    onChange={onFormChange(setBookingGuestForm)}
                  />
                </Field>
                <Field label="Оплата">
                  <select
                    name="paymentMethod"
                    value={bookingGuestForm.paymentMethod}
                    onChange={onFormChange(setBookingGuestForm)}
                  >
                    <option value="at_hotel">При заселении</option>
                    <option value="online_hold">Картой после подтверждения</option>
                  </select>
                </Field>
              </div>

              <aside className="booking-summary">
                {renderImageCarousel(
                  getRoomImages(bookingModalRoom),
                  `Номер ${bookingModalRoom.number}`,
                  `booking-modal-room-${bookingModalRoom.id}`,
                )}
                {renderPhotoStrip(getRoomImages(bookingModalRoom), `Номер ${bookingModalRoom.number}`)}
                <h3>Номер {bookingModalRoom.number}</h3>
                <p className="muted">
                  {roomTypeLabel(bookingModalRoom.type)} · {guestCountLabel(bookingModalRoom.capacity || 1)}
                </p>
                <div className="mini-block">
                  <strong>{hotelFilters.checkInDate} - {hotelFilters.checkOutDate}</strong>
                  <span>{formatPrice(bookingModalRoom.price)} за ночь</span>
                </div>
                <button className="primary-button wide" disabled={busy} type="submit">
                  Подтвердить бронирование
                </button>
              </aside>
            </form>
          </section>
        </div>
      )}
    </main>
  )
}

export default App

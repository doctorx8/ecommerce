import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api, money, type Address, type CustomerProfile, type Order } from '../api/client'
import { useStore } from '../state/StoreContext'

type Tab = 'profile' | 'password' | 'addresses' | 'orders' | 'danger'

const emptyAddress = {
  firstName: '',
  lastName: '',
  company: '',
  address1: '',
  address2: '',
  city: '',
  postcode: '',
  country: 'US',
  zone: '',
  isDefault: false,
}

export function AccountPage() {
  const { user, setUser, logout } = useStore()
  const navigate = useNavigate()
  const [tab, setTab] = useState<Tab>('profile')
  const [profile, setProfile] = useState<CustomerProfile | null>(null)
  const [orders, setOrders] = useState<Order[]>([])
  const [addresses, setAddresses] = useState<Address[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const [profileForm, setProfileForm] = useState({
    email: '',
    firstName: '',
    lastName: '',
    telephone: '',
    newsletter: false,
  })
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [addressForm, setAddressForm] = useState(emptyAddress)
  const [editingAddressId, setEditingAddressId] = useState<number | null>(null)
  const [deletePassword, setDeletePassword] = useState('')

  useEffect(() => {
    if (!user) return
    Promise.all([api.me(), api.myOrders(), api.getAddresses()])
      .then(([me, orderList, addressList]) => {
        setProfile(me)
        setOrders(orderList)
        setAddresses(addressList)
        setProfileForm({
          email: me.email,
          firstName: me.firstName,
          lastName: me.lastName,
          telephone: me.telephone || '',
          newsletter: Boolean(me.newsletter),
        })
      })
      .catch((err: Error) => setError(err.message))
  }, [user])

  if (!user) return <Navigate to="/auth" replace />

  function clearFeedback() {
    setError('')
    setMessage('')
  }

  async function saveProfile(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    clearFeedback()
    try {
      const updated = await api.updateProfile(profileForm)
      setProfile(updated)
      setUser({
        id: updated.id,
        email: updated.email,
        firstName: updated.firstName,
        lastName: updated.lastName,
      })
      setMessage('Account information saved.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save profile')
    } finally {
      setBusy(false)
    }
  }

  async function savePassword(e: FormEvent) {
    e.preventDefault()
    clearFeedback()
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('New passwords do not match')
      return
    }
    setBusy(true)
    try {
      await api.changePassword(passwordForm.currentPassword, passwordForm.newPassword)
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setMessage('Password updated.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not change password')
    } finally {
      setBusy(false)
    }
  }

  async function saveAddress(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    clearFeedback()
    try {
      const payload = {
        ...addressForm,
        company: addressForm.company || undefined,
        address2: addressForm.address2 || undefined,
        zone: addressForm.zone || undefined,
      }
      if (editingAddressId) {
        await api.updateAddress(editingAddressId, payload as Omit<Address, 'id'>)
        setMessage('Address updated.')
      } else {
        await api.createAddress(payload as Omit<Address, 'id'>)
        setMessage('Address added.')
      }
      setAddresses(await api.getAddresses())
      setAddressForm(emptyAddress)
      setEditingAddressId(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save address')
    } finally {
      setBusy(false)
    }
  }

  async function removeAddress(id: number) {
    clearFeedback()
    try {
      await api.deleteAddress(id)
      setAddresses(await api.getAddresses())
      setMessage('Address removed.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete address')
    }
  }

  function startEditAddress(address: Address) {
    setEditingAddressId(address.id)
    setAddressForm({
      firstName: address.firstName,
      lastName: address.lastName,
      company: address.company || '',
      address1: address.address1,
      address2: address.address2 || '',
      city: address.city,
      postcode: address.postcode,
      country: address.country,
      zone: address.zone || '',
      isDefault: address.isDefault,
    })
    setTab('addresses')
  }

  async function deleteAccount(e: FormEvent) {
    e.preventDefault()
    if (!window.confirm('Delete your Karwan account permanently? This cannot be undone.')) {
      return
    }
    setBusy(true)
    clearFeedback()
    try {
      await api.deleteAccount(deletePassword)
      logout()
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete account')
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <div className="container">
        <h1 className="page-title">Account</h1>
        <p className="muted">
          Manage your Karwan profile, addresses, and orders
          {profile ? ` · Member since ${new Date(profile.createdAt || '').toLocaleDateString()}` : ''}
        </p>

        <div className="account-layout">
          <aside className="account-nav">
            {(
              [
                ['profile', 'Account info'],
                ['password', 'Change password'],
                ['addresses', 'Addresses'],
                ['orders', 'Orders'],
                ['danger', 'Delete account'],
              ] as const
            ).map(([key, label]) => (
              <button
                key={key}
                type="button"
                className={tab === key ? 'active' : ''}
                onClick={() => {
                  clearFeedback()
                  setTab(key)
                }}
              >
                {label}
              </button>
            ))}
          </aside>

          <section className="account-panel">
            {error ? <div className="alert">{error}</div> : null}
            {message ? <div className="success">{message}</div> : null}

            {tab === 'profile' ? (
              <form className="form-grid" onSubmit={saveProfile}>
                <h2>Account information</h2>
                <label>
                  First name
                  <input
                    required
                    value={profileForm.firstName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                  />
                </label>
                <label>
                  Last name
                  <input
                    required
                    value={profileForm.lastName}
                    onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })}
                  />
                </label>
                <label className="full">
                  Email
                  <input
                    required
                    type="email"
                    value={profileForm.email}
                    onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                  />
                </label>
                <label className="full">
                  Phone
                  <input
                    value={profileForm.telephone}
                    onChange={(e) => setProfileForm({ ...profileForm, telephone: e.target.value })}
                  />
                </label>
                <label className="full checkbox-row">
                  <input
                    type="checkbox"
                    checked={profileForm.newsletter}
                    onChange={(e) => setProfileForm({ ...profileForm, newsletter: e.target.checked })}
                  />
                  Subscribe to newsletter
                </label>
                <div className="full">
                  <button className="btn btn-primary" type="submit" disabled={busy}>
                    {busy ? 'Saving…' : 'Save changes'}
                  </button>
                </div>
              </form>
            ) : null}

            {tab === 'password' ? (
              <form className="form-grid" onSubmit={savePassword} style={{ maxWidth: 480 }}>
                <h2>Change password</h2>
                <label className="full">
                  Current password
                  <input
                    required
                    type="password"
                    value={passwordForm.currentPassword}
                    onChange={(e) =>
                      setPasswordForm({ ...passwordForm, currentPassword: e.target.value })
                    }
                  />
                </label>
                <label className="full">
                  New password
                  <input
                    required
                    type="password"
                    minLength={8}
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  />
                </label>
                <label className="full">
                  Confirm new password
                  <input
                    required
                    type="password"
                    minLength={8}
                    value={passwordForm.confirmPassword}
                    onChange={(e) =>
                      setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
                    }
                  />
                </label>
                <div className="full">
                  <button className="btn btn-primary" type="submit" disabled={busy}>
                    {busy ? 'Updating…' : 'Update password'}
                  </button>
                </div>
              </form>
            ) : null}

            {tab === 'addresses' ? (
              <div>
                <h2>Addresses</h2>
                <div className="address-list">
                  {addresses.length === 0 ? (
                    <p className="muted">No saved addresses yet.</p>
                  ) : (
                    addresses.map((address) => (
                      <div className="address-card" key={address.id}>
                        <div>
                          <strong>
                            {address.firstName} {address.lastName}
                            {address.isDefault ? ' · Default' : ''}
                          </strong>
                          <p className="muted" style={{ margin: '0.35rem 0 0' }}>
                            {address.address1}
                            {address.address2 ? `, ${address.address2}` : ''}
                            <br />
                            {address.city}, {address.zone || ''} {address.postcode}
                            <br />
                            {address.country}
                          </p>
                        </div>
                        <div className="address-actions">
                          <button type="button" className="linkish" onClick={() => startEditAddress(address)}>
                            Edit
                          </button>
                          <button type="button" className="linkish" onClick={() => removeAddress(address.id)}>
                            Delete
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                <form className="form-grid" onSubmit={saveAddress} style={{ marginTop: '1.5rem' }}>
                  <h3 style={{ gridColumn: '1 / -1', margin: 0 }}>
                    {editingAddressId ? 'Edit address' : 'Add address'}
                  </h3>
                  <label>
                    First name
                    <input
                      required
                      value={addressForm.firstName}
                      onChange={(e) => setAddressForm({ ...addressForm, firstName: e.target.value })}
                    />
                  </label>
                  <label>
                    Last name
                    <input
                      required
                      value={addressForm.lastName}
                      onChange={(e) => setAddressForm({ ...addressForm, lastName: e.target.value })}
                    />
                  </label>
                  <label className="full">
                    Company
                    <input
                      value={addressForm.company}
                      onChange={(e) => setAddressForm({ ...addressForm, company: e.target.value })}
                    />
                  </label>
                  <label className="full">
                    Address
                    <input
                      required
                      value={addressForm.address1}
                      onChange={(e) => setAddressForm({ ...addressForm, address1: e.target.value })}
                    />
                  </label>
                  <label className="full">
                    Address line 2
                    <input
                      value={addressForm.address2}
                      onChange={(e) => setAddressForm({ ...addressForm, address2: e.target.value })}
                    />
                  </label>
                  <label>
                    City
                    <input
                      required
                      value={addressForm.city}
                      onChange={(e) => setAddressForm({ ...addressForm, city: e.target.value })}
                    />
                  </label>
                  <label>
                    Postcode
                    <input
                      required
                      value={addressForm.postcode}
                      onChange={(e) => setAddressForm({ ...addressForm, postcode: e.target.value })}
                    />
                  </label>
                  <label>
                    Country
                    <input
                      required
                      value={addressForm.country}
                      onChange={(e) => setAddressForm({ ...addressForm, country: e.target.value })}
                    />
                  </label>
                  <label>
                    State / Zone
                    <input
                      value={addressForm.zone}
                      onChange={(e) => setAddressForm({ ...addressForm, zone: e.target.value })}
                    />
                  </label>
                  <label className="full checkbox-row">
                    <input
                      type="checkbox"
                      checked={addressForm.isDefault}
                      onChange={(e) => setAddressForm({ ...addressForm, isDefault: e.target.checked })}
                    />
                    Set as default address
                  </label>
                  <div className="full" style={{ display: 'flex', gap: '0.75rem' }}>
                    <button className="btn btn-primary" type="submit" disabled={busy}>
                      {busy ? 'Saving…' : editingAddressId ? 'Update address' : 'Add address'}
                    </button>
                    {editingAddressId ? (
                      <button
                        className="btn btn-ghost"
                        type="button"
                        onClick={() => {
                          setEditingAddressId(null)
                          setAddressForm(emptyAddress)
                        }}
                      >
                        Cancel
                      </button>
                    ) : null}
                  </div>
                </form>
              </div>
            ) : null}

            {tab === 'orders' ? (
              <div>
                <h2>Orders</h2>
                {orders.length === 0 ? (
                  <div className="empty">
                    No orders yet. <Link to="/shop">Start shopping</Link>
                  </div>
                ) : (
                  <div className="order-list panel">
                    {orders.map((order) => (
                      <div
                        className="order-row"
                        key={order.id}
                        style={{ gridTemplateColumns: '1fr auto' }}
                      >
                        <div>
                          <Link to={`/order/${order.id}`}>
                            <strong>{order.orderNumber}</strong>
                          </Link>
                          <div className="muted">
                            {order.status}
                            {order.createdAt
                              ? ` · ${new Date(order.createdAt).toLocaleDateString()}`
                              : ''}
                          </div>
                        </div>
                        <div>{money(order.total)}</div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : null}

            {tab === 'danger' ? (
              <form className="form-grid danger-box" onSubmit={deleteAccount} style={{ maxWidth: 480 }}>
                <h2>Delete account</h2>
                <p className="muted full">
                  This deactivates your account and signs you out. Enter your password to confirm.
                </p>
                <label className="full">
                  Password
                  <input
                    required
                    type="password"
                    value={deletePassword}
                    onChange={(e) => setDeletePassword(e.target.value)}
                  />
                </label>
                <div className="full">
                  <button className="btn btn-danger" type="submit" disabled={busy}>
                    {busy ? 'Deleting…' : 'Delete my account'}
                  </button>
                </div>
              </form>
            ) : null}
          </section>
        </div>
      </div>
    </div>
  )
}

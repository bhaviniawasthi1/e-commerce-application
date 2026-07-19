import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="bg-white shadow-md border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="text-2xl font-bold text-indigo-600">Velora</Link>
            <div className="hidden md:flex ml-10 space-x-6">
              <Link to="/products" className="text-gray-700 hover:text-indigo-600 px-3 py-2">Products</Link>
              {user && <Link to="/cart" className="text-gray-700 hover:text-indigo-600 px-3 py-2">Cart</Link>}
              {user && <Link to="/orders" className="text-gray-700 hover:text-indigo-600 px-3 py-2">Orders</Link>}
              {user?.role === 'ADMIN' && (
                <Link to="/admin" className="text-gray-700 hover:text-indigo-600 px-3 py-2">Admin</Link>
              )}
            </div>
          </div>
          <div className="flex items-center space-x-4">
            {user ? (
              <div className="flex items-center space-x-4">
                <span className="hidden md:block text-sm text-gray-600">
                  {user.firstName || user.username}
                  {user.role === 'ADMIN' && <span className="ml-1 text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded">ADMIN</span>}
                </span>
                <button onClick={handleLogout} className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm">Logout</button>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link to="/login" className="text-gray-700 hover:text-indigo-600 px-3 py-2 text-sm">Login</Link>
                <Link to="/register" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm">Register</Link>
              </div>
            )}
            <button onClick={() => setOpen(!open)} className="md:hidden p-2 rounded text-gray-600 hover:bg-gray-100">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
            </button>
          </div>
        </div>
      </div>
      {open && (
        <div className="md:hidden bg-white border-t pb-4 px-4 space-y-2">
          <Link to="/products" className="block text-gray-700 py-2" onClick={() => setOpen(false)}>Products</Link>
          {user && <Link to="/cart" className="block text-gray-700 py-2" onClick={() => setOpen(false)}>Cart</Link>}
          {user && <Link to="/orders" className="block text-gray-700 py-2" onClick={() => setOpen(false)}>Orders</Link>}
          {user?.role === 'ADMIN' && <Link to="/admin" className="block text-gray-700 py-2" onClick={() => setOpen(false)}>Admin</Link>}
        </div>
      )}
    </nav>
  )
}

import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'

export default function Cart() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadCart = () => {
    api.get('/cart').then(res => setCart(res.data.data)).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(loadCart, [])

  const updateQty = async (productId, quantity) => {
    if (quantity <= 0) {
      await api.delete(`/cart/items/${productId}`)
    } else {
      await api.put(`/cart/items/${productId}?quantity=${quantity}`)
    }
    loadCart()
  }

  const removeItem = async (productId) => {
    await api.delete(`/cart/items/${productId}`)
    loadCart()
  }

  const clearCart = async () => {
    await api.delete('/cart')
    loadCart()
  }

  if (loading) return <div className="flex justify-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Shopping Cart</h1>
      {!cart || cart.items.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-xl border border-gray-100">
          <svg className="w-16 h-16 mx-auto text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" /></svg>
          <p className="text-xl text-gray-500 mt-4">Your cart is empty</p>
          <Link to="/products" className="inline-block mt-4 bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700">Browse Products</Link>
        </div>
      ) : (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="hidden md:grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 text-sm font-medium text-gray-500">
              <div className="col-span-5">Product</div>
              <div className="col-span-2 text-center">Price</div>
              <div className="col-span-2 text-center">Quantity</div>
              <div className="col-span-2 text-center">Subtotal</div>
              <div className="col-span-1"></div>
            </div>
            {cart.items.map(item => (
              <div key={item.productId} className="grid grid-cols-12 gap-4 items-center px-6 py-4 border-t border-gray-100">
                <div className="col-span-12 md:col-span-5 flex items-center gap-3">
                  <div className="w-16 h-16 bg-gray-100 rounded flex items-center justify-center flex-shrink-0">
                    {item.productImage ? <img src={item.productImage} alt="" className="w-full h-full object-cover rounded" /> : <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{item.productName}</p>
                    <p className="text-sm text-gray-500">${item.unitPrice}</p>
                  </div>
                </div>
                <div className="col-span-4 md:col-span-2 text-center text-gray-900 font-medium">${item.unitPrice}</div>
                <div className="col-span-4 md:col-span-2 flex justify-center">
                  <div className="flex items-center border rounded-lg">
                    <button onClick={() => updateQty(item.productId, item.quantity - 1)} className="px-2 py-1 hover:bg-gray-50">-</button>
                    <span className="px-3 py-1 border-x text-gray-900">{item.quantity}</span>
                    <button onClick={() => updateQty(item.productId, item.quantity + 1)} className="px-2 py-1 hover:bg-gray-50">+</button>
                  </div>
                </div>
                <div className="col-span-3 md:col-span-2 text-center text-gray-900 font-medium">${item.subtotal}</div>
                <div className="col-span-1 flex justify-end">
                  <button onClick={() => removeItem(item.productId)} className="text-red-500 hover:text-red-700">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-6 gap-4">
            <button onClick={clearCart} className="text-red-600 hover:text-red-700 text-sm font-medium">Clear Cart</button>
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 w-full sm:w-auto">
              <div className="flex justify-between items-center mb-4">
                <span className="text-gray-600">Total Items:</span>
                <span className="font-medium text-gray-900">{cart.totalItems}</span>
              </div>
              <div className="flex justify-between items-center mb-4">
                <span className="text-lg text-gray-900 font-semibold">Total:</span>
                <span className="text-2xl font-bold text-indigo-600">${cart.totalPrice}</span>
              </div>
              <button onClick={() => navigate('/checkout')}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white py-3 rounded-lg font-medium">
                Proceed to Checkout
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

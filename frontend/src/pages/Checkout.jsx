import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'

const PAYMENT_METHODS = ['UPI', 'CARD', 'NET_BANKING', 'COD']

export default function Checkout() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [form, setForm] = useState({ paymentMethod: 'UPI', shippingAddress: '' })
  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/cart').then(res => {
      if (!res.data.data || res.data.data.items.length === 0) { navigate('/cart'); return }
      setCart(res.data.data)
    }).catch(() => navigate('/cart'))
  }, [])

  const placeOrder = async (e) => {
    e.preventDefault()
    setError('')
    setPlacing(true)
    try {
      const res = await api.post('/orders', form)
      navigate(`/orders/${res.data.data.id}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order')
    } finally { setPlacing(false) }
  }

  if (!cart) return null

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Checkout</h1>
      <div className="grid md:grid-cols-5 gap-8">
        <div className="md:col-span-3">
          <form onSubmit={placeOrder} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Payment Method</label>
              <div className="grid grid-cols-2 gap-3">
                {PAYMENT_METHODS.map(m => (
                  <button key={m} type="button" onClick={() => setForm({ ...form, paymentMethod: m })}
                    className={`px-4 py-3 rounded-lg border text-sm font-medium transition ${form.paymentMethod === m ? 'border-indigo-600 bg-indigo-50 text-indigo-700' : 'border-gray-200 text-gray-600 hover:border-gray-300'}`}>
                    {m === 'UPI' ? 'UPI' : m === 'CARD' ? 'Card' : m === 'NET_BANKING' ? 'Net Banking' : 'Cash on Delivery'}
                  </button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Shipping Address</label>
              <textarea required rows={3} value={form.shippingAddress} onChange={e => setForm({ ...form, shippingAddress: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" placeholder="Enter your full shipping address" />
            </div>
            {error && <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm">{error}</div>}
            <button type="submit" disabled={placing}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white py-3 rounded-lg font-medium disabled:opacity-50">
              {placing ? 'Placing order...' : `Place Order - $${cart.totalPrice}`}
            </button>
          </form>
        </div>
        <div className="md:col-span-2">
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <h3 className="font-semibold text-gray-900 mb-4">Order Summary</h3>
            {cart.items.map(item => (
              <div key={item.productId} className="flex justify-between py-2 border-b border-gray-100 text-sm">
                <span className="text-gray-600">{item.productName} × {item.quantity}</span>
                <span className="text-gray-900 font-medium">${item.subtotal}</span>
              </div>
            ))}
            <div className="flex justify-between pt-4 mt-2">
              <span className="font-semibold text-gray-900">Total</span>
              <span className="text-xl font-bold text-indigo-600">${cart.totalPrice}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

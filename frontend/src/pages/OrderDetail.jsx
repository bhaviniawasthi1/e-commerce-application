import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api/axios'

const STATUS_COLORS = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  SHIPPED: 'bg-indigo-100 text-indigo-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700'
}

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED']

export default function OrderDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [cancelling, setCancelling] = useState(false)

  useEffect(() => {
    api.get(`/orders/${id}`).then(res => setOrder(res.data.data)).catch(() => navigate('/orders')).finally(() => setLoading(false))
  }, [id])

  const cancelOrder = async () => {
    if (!confirm('Are you sure you want to cancel this order?')) return
    setCancelling(true)
    try {
      const res = await api.put(`/orders/${id}/cancel`)
      setOrder(res.data.data)
    } catch (err) { alert(err.response?.data?.message || 'Failed to cancel') }
    finally { setCancelling(false) }
  }

  if (loading) return <div className="flex justify-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div></div>
  if (!order) return null

  const steps = STATUS_STEPS.map(s => ({ name: s, active: STATUS_STEPS.indexOf(s) <= STATUS_STEPS.indexOf(order.status) }))

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <button onClick={() => navigate('/orders')} className="text-indigo-600 hover:text-indigo-700 mb-4 flex items-center gap-1">
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
        Back to Orders
      </button>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
        <div className="flex flex-wrap justify-between items-start gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Order #{order.orderNumber}</h1>
            <p className="text-gray-500 mt-1">Placed on {new Date(order.createdAt).toLocaleDateString()} at {new Date(order.createdAt).toLocaleTimeString()}</p>
          </div>
          <div className="text-right">
            <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLORS[order.status] || 'bg-gray-100'}`}>{order.status}</span>
            <p className="text-sm text-gray-500 mt-1">{order.paymentMethod} · {order.paymentStatus}</p>
          </div>
        </div>

        {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
          <div className="mt-6">
            <div className="flex items-center justify-between">
              {steps.map((s, i) => (
                <div key={s.name} className="flex items-center flex-1">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${s.active ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-500'}`}>{i + 1}</div>
                  <div className={`ml-2 text-xs font-medium ${s.active ? 'text-indigo-600' : 'text-gray-400'}`}>{s.name}</div>
                  {i < steps.length - 1 && <div className={`flex-1 h-0.5 mx-2 ${s.active ? 'bg-indigo-600' : 'bg-gray-200'}`} />}
                </div>
              ))}
            </div>
          </div>
        )}

        {['PENDING', 'CONFIRMED'].includes(order.status) && (
          <button onClick={cancelOrder} disabled={cancelling}
            className="mt-6 bg-red-600 hover:bg-red-700 text-white px-6 py-2 rounded-lg text-sm font-medium disabled:opacity-50">
            {cancelling ? 'Cancelling...' : 'Cancel Order'}
          </button>
        )}

        <div className="mt-6 p-4 bg-gray-50 rounded-lg">
          <h4 className="text-sm font-medium text-gray-700">Shipping Address</h4>
          <p className="text-gray-600 mt-1">{order.shippingAddress}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-3 bg-gray-50 text-sm font-medium text-gray-500">Items</div>
        {order.items.map(item => (
          <div key={item.id} className="flex items-center justify-between px-6 py-4 border-t border-gray-100">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-gray-100 rounded flex items-center justify-center flex-shrink-0">
                {item.productImage ? <img src={item.productImage} alt="" className="w-full h-full object-cover rounded" /> : <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg>}
              </div>
              <div>
                <p className="font-medium text-gray-900">{item.productName}</p>
                <p className="text-sm text-gray-500">${item.unitPrice} × {item.quantity}</p>
              </div>
            </div>
            <span className="font-medium text-gray-900">${item.subtotal}</span>
          </div>
        ))}
        <div className="flex justify-between px-6 py-4 border-t border-gray-100 bg-gray-50">
          <span className="font-semibold text-gray-900">Total</span>
          <span className="text-xl font-bold text-indigo-600">${order.totalAmount}</span>
        </div>
      </div>
    </div>
  )
}

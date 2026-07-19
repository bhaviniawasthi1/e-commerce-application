import { useState, useEffect } from 'react'
import api from '../../api/axios'

const STATUS_OPTIONS = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']
const STATUS_COLORS = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  SHIPPED: 'bg-indigo-100 text-indigo-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700'
}

export default function ManageOrders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const loadOrders = () => {
    setLoading(true)
    api.get(`/orders/admin?page=${page}&size=10`).then(res => {
      setOrders(res.data.data.content)
      setTotalPages(res.data.data.totalPages)
    }).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(loadOrders, [page])

  const updateStatus = async (id, status) => {
    try {
      await api.put(`/orders/${id}/status?status=${status}`)
      loadOrders()
    } catch (err) { alert(err.response?.data?.message || 'Failed to update status') }
  }

  const cancelOrder = async (id) => {
    if (!confirm('Cancel this order?')) return
    try { await api.put(`/orders/${id}/cancel`); loadOrders() }
    catch (err) { alert(err.response?.data?.message || 'Failed to cancel') }
  }

  if (loading) return <div className="flex justify-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div></div>

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Orders</h1>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="text-left text-sm text-gray-500 border-b border-gray-100 bg-gray-50">
              <th className="px-6 py-3 font-medium">Order #</th>
              <th className="px-6 py-3 font-medium">Total</th>
              <th className="px-6 py-3 font-medium">Payment</th>
              <th className="px-6 py-3 font-medium">Status</th>
              <th className="px-6 py-3 font-medium">Date</th>
              <th className="px-6 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => (
              <tr key={order.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="px-6 py-4 font-medium text-gray-900 text-sm">{order.orderNumber}</td>
                <td className="px-6 py-4 text-gray-900 font-medium">${order.totalAmount}</td>
                <td className="px-6 py-4 text-sm text-gray-500">{order.paymentMethod}<br /><span className="text-xs">{order.paymentStatus}</span></td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${STATUS_COLORS[order.status] || 'bg-gray-100'}`}>{order.status}</span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                <td className="px-6 py-4 text-right">
                  <div className="flex items-center justify-end gap-2">
                    <select value={order.status} onChange={e => updateStatus(order.id, e.target.value)}
                      className="text-sm border border-gray-300 rounded px-2 py-1 focus:ring-2 focus:ring-indigo-500 outline-none">
                      {STATUS_OPTIONS.filter(s => s !== 'CANCELLED').map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                    {['PENDING', 'CONFIRMED'].includes(order.status) && (
                      <button onClick={() => cancelOrder(order.id)} className="text-red-600 hover:text-red-700 text-sm">Cancel</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders.length === 0 && <p className="text-center py-8 text-gray-500">No orders yet</p>}
      </div>
      {totalPages > 1 && (
        <div className="flex justify-center items-center space-x-2 mt-6">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-4 py-2 bg-white border rounded-lg disabled:opacity-50 hover:bg-gray-50">Previous</button>
          <span className="text-gray-500 text-sm">Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-4 py-2 bg-white border rounded-lg disabled:opacity-50 hover:bg-gray-50">Next</button>
        </div>
      )}
    </div>
  )
}

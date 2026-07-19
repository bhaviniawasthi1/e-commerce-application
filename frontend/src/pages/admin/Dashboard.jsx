import { useState, useEffect } from 'react'
import api from '../../api/axios'

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/admin/dashboard').then(res => setData(res.data.data)).catch(() => {}).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div></div>
  if (!data) return <div className="text-center py-20 text-red-500">Failed to load dashboard</div>

  const cards = [
    { label: 'Total Users', value: data.totalUsers, color: 'bg-blue-500' },
    { label: 'Customers', value: data.totalCustomers, color: 'bg-green-500' },
    { label: 'Admins', value: data.totalAdmins, color: 'bg-purple-500' },
    { label: 'Products', value: data.totalProducts, color: 'bg-indigo-500' },
    { label: 'Orders', value: data.totalOrders, color: 'bg-orange-500' },
    { label: 'Revenue', value: `$${data.totalRevenue}`, color: 'bg-emerald-500' },
    { label: 'Low Stock Items', value: data.lowStockProducts, color: 'bg-red-500' },
  ]

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
        {cards.map(card => (
          <div key={card.label} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center gap-3">
              <div className={`w-3 h-3 rounded-full ${card.color}`}></div>
              <p className="text-sm text-gray-500">{card.label}</p>
            </div>
            <p className="text-3xl font-bold text-gray-900 mt-3">{card.value}</p>
          </div>
        ))}
      </div>

      {data.topSellingProducts?.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Top Selling Products</h2>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="text-left text-sm text-gray-500 border-b border-gray-100">
                  <th className="pb-3 font-medium">Product</th>
                  <th className="pb-3 font-medium">Quantity Sold</th>
                  <th className="pb-3 font-medium">Revenue</th>
                </tr>
              </thead>
              <tbody>
                {data.topSellingProducts.map(p => (
                  <tr key={p.productId} className="border-b border-gray-50">
                    <td className="py-3 text-gray-900 font-medium">{p.productName}</td>
                    <td className="py-3 text-gray-600">{p.totalQuantitySold}</td>
                    <td className="py-3 text-gray-900 font-medium">${p.totalRevenue}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

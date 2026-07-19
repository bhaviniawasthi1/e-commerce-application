import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'

export default function Products() {
  const [products, setProducts] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [categories, setCategories] = useState([])
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/categories?size=100').then(res => setCategories(res.data.data.content)).catch(() => {})
  }, [])

  useEffect(() => {
    setLoading(true)
    const params = { page, size: 8, sortBy, sortDir }
    if (keyword) params.keyword = keyword
    if (categoryId) params.categoryId = categoryId
    api.get('/products', { params })
      .then(res => { setProducts(res.data.data.content); setTotalPages(res.data.data.totalPages) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [page, keyword, categoryId, sortBy, sortDir])

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Products</h1>
      <div className="flex flex-wrap gap-4 mb-6">
        <input type="text" placeholder="Search products..." value={keyword}
          onChange={e => { setKeyword(e.target.value); setPage(0) }}
          className="flex-1 min-w-[200px] px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" />
        <select value={categoryId} onChange={e => { setCategoryId(e.target.value); setPage(0) }}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none">
          <option value="">All Categories</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={`${sortBy}-${sortDir}`} onChange={e => { const [b, d] = e.target.value.split('-'); setSortBy(b); setSortDir(d) }}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none">
          <option value="createdAt-desc">Newest</option>
          <option value="createdAt-asc">Oldest</option>
          <option value="price-asc">Price: Low to High</option>
          <option value="price-desc">Price: High to Low</option>
          <option value="name-asc">Name: A-Z</option>
          <option value="name-desc">Name: Z-A</option>
        </select>
      </div>

      {loading ? (
        <div className="flex justify-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div></div>
      ) : products.length === 0 ? (
        <div className="text-center py-20 text-gray-500"><p className="text-xl">No products found</p></div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {products.map(p => (
              <Link key={p.id} to={`/products/${p.id}`} className="bg-white rounded-xl shadow-sm hover:shadow-md transition border border-gray-100 overflow-hidden group">
                <div className="h-48 bg-gray-100 flex items-center justify-center">
                  {p.imageUrl ? (
                    <img src={p.imageUrl} alt={p.name} className="w-full h-full object-cover group-hover:scale-105 transition" />
                  ) : (
                    <div className="text-gray-400"><svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg></div>
                  )}
                </div>
                <div className="p-4">
                  <p className="text-xs text-indigo-600 font-medium mb-1">{p.category?.name}</p>
                  <h3 className="font-semibold text-gray-900 truncate">{p.name}</h3>
                  <p className="text-sm text-gray-500 mt-1 line-clamp-2">{p.description}</p>
                  <div className="flex items-center justify-between mt-3">
                    <span className="text-xl font-bold text-gray-900">${p.price}</span>
                    <span className={`text-xs font-medium px-2 py-1 rounded ${p.stock > 0 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {p.stock > 0 ? `${p.stock} in stock` : 'Out of stock'}
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex justify-center items-center space-x-2 mt-8">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                className="px-4 py-2 bg-white border rounded-lg disabled:opacity-50 hover:bg-gray-50">Previous</button>
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i} onClick={() => setPage(i)}
                  className={`px-4 py-2 rounded-lg ${page === i ? 'bg-indigo-600 text-white' : 'bg-white border hover:bg-gray-50'}`}>{i + 1}</button>
              ))}
              <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
                className="px-4 py-2 bg-white border rounded-lg disabled:opacity-50 hover:bg-gray-50">Next</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

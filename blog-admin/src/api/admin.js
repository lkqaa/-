import http from './http'

export const articleApi = {
  list: params => http.get('/admin/articles', { params }),
  detail: id => http.get(`/admin/articles/${id}`),
  create: data => http.post('/admin/articles', data),
  update: (id, data) => http.put(`/admin/articles/${id}`, data),
  updateStatus: (id, status) => http.put(`/admin/articles/${id}/status`, { status }),
  remove: id => http.delete(`/admin/articles/${id}`),
  batchRemove: ids => http.post('/admin/articles/batch-delete', { ids })
}

export const categoryApi = {
  list: () => http.get('/admin/categories'),
  create: data => http.post('/admin/categories', data),
  update: (id, data) => http.put(`/admin/categories/${id}`, data),
  remove: id => http.delete(`/admin/categories/${id}`)
}

export const tagApi = {
  list: () => http.get('/admin/tags'),
  create: data => http.post('/admin/tags', data),
  update: (id, data) => http.put(`/admin/tags/${id}`, data),
  remove: id => http.delete(`/admin/tags/${id}`)
}

export const commentApi = {
  list: params => http.get('/admin/comments', { params }),
  updateStatus: (id, status) => http.put(`/admin/comments/${id}/status`, { status }),
  remove: id => http.delete(`/admin/comments/${id}`),
  batchRemove: ids => http.post('/admin/comments/batch-delete', { ids })
}

export const statsApi = {
  overview: () => http.get('/admin/stats/overview'),
  trend: days => http.get('/admin/stats/trend', { params: { days } }),
  hotArticles: limit => http.get('/admin/stats/hot-articles', { params: { limit } })
}

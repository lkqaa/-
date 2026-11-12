import http from './http'

export const articleApi = {
  list: params => http.get('/articles', { params }),
  detail: id => http.get(`/articles/${id}`),
  view: id => http.post(`/articles/${id}/view`),
  comments: (id, params) => http.get(`/articles/${id}/comments`, { params }),
  addComment: (id, data) => http.post(`/articles/${id}/comments`, data)
}

export const siteApi = {
  categories: () => http.get('/categories'),
  tags: () => http.get('/tags'),
  config: () => http.get('/config')
}

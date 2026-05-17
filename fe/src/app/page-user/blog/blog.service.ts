import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Blog } from './blog.model';
@Injectable({
  providedIn: 'root',
})
export class BlogService {
  private blogs: Blog[] = [
    {
      id: 1,
      title: 'Top local brand Việt Nam hot nhất 2026',
      slug: 'top-local-brand-viet-nam',
      thumbnail: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b',
      description:
        'Tổng hợp những local brand đang được giới trẻ yêu thích nhất hiện nay.',
      createdAt: '18/05/2026',
      author: 'Admin',
      category: 'Fashion',
    },
    {
      id: 2,
      title: 'Cách phối hoodie oversize chuẩn streetwear',
      slug: 'phoi-hoodie-oversize',
      thumbnail: 'https://images.unsplash.com/photo-1496747611176-843222e1e57c',
      description: 'Gợi ý outfit streetwear cực chất dành cho nam và nữ.',
      createdAt: '18/05/2026',
      author: 'Admin',
      category: 'Style',
    },
    {
      id: 3,
      title: 'Xu hướng thời trang mùa hè mới nhất',
      slug: 'xuan-huong-thoi-trang-he',
      thumbnail: 'https://images.unsplash.com/photo-1483985988355-763728e1935b',
      description: 'Những item không thể thiếu trong mùa hè năm nay.',
      createdAt: '18/05/2026',
      author: 'Admin',
      category: 'Trend',
    },
    {
      id: 4,
      title: 'Top sneaker đáng mua dưới 3 triệu',
      slug: 'top-sneaker-duoi-3-trieu',
      thumbnail: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff',
      description: 'Danh sách sneaker đẹp và đáng tiền cho học sinh sinh viên.',
      createdAt: '18/05/2026',
      author: 'Admin',
      category: 'Sneaker',
    },
  ];

  getBlogs(): Observable<Blog[]> {
    return of(this.blogs);
  }
}

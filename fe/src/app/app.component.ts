import { Component, OnInit } from '@angular/core';
import { AuthService, User } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  title = 'wukong-shop';
  currentUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        this.currentUser = user;
        console.log('User loaded on app init:', user);
      },
      error: (err) => {
        console.error('Error loading user on app init:', err);
      },
    });
  }
}

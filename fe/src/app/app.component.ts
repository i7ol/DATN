import { Component, OnInit } from '@angular/core';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  title = 'wukong-shop';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Kiểm tra nếu đã có token thì load user profile
    if (this.authService.isAuthenticated()) {
      this.authService.loadUserProfile().subscribe();
    }
  }
}

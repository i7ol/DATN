// app/shared/components/navbar/navbar.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { User } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      this.isAdmin = this.authService.isAdmin();
    });
  }

  logout(): void {
    this.authService.logout();
  }

  navigateToAdmin(): void {
    this.router.navigate(['/admin']);
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './layout/header/header';
import { SidebarComponent } from './layout/sidebar/sidebar';
import { LoginComponent } from './auth/login/login';
import { RegisterComponent } from './auth/register/register';
import { AuthService, User } from './services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet, 
    HeaderComponent, 
    SidebarComponent, 
    LoginComponent, 
    RegisterComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  user: User | null = null;
  showAuth = false;
  authMode: 'login' | 'register' = 'login';
  sidebarOpen = true;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.user = user;
      if (user) {
        this.showAuth = false;
      }
    });
  }

  showLoginModal(): void {
    this.authMode = 'login';
    this.showAuth = true;
  }

  closeAuthModal(): void {
    this.showAuth = false;
  }

  switchToRegister(): void {
    this.authMode = 'register';
  }

  switchToLogin(): void {
    this.authMode = 'login';
  }
}
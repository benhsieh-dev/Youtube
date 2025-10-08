import { Component, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, LoginRequest } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  @Output() switchToRegister = new EventEmitter<void>();

  loginForm: FormGroup;
  loading = false;
  error = '';

  private demoCredentials = {
    username: 'demo',
    password: 'demo123'
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  fillDemoCredentials(): void {
    this.loginForm.patchValue(
      {
        username: this.demoCredentials.username,
        password: this.demoCredentials.password
      });
  }
  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.error = '';

      const credentials: LoginRequest = this.loginForm.value;

      this.authService.login(credentials).subscribe({
        next: (user) => {
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Invalid credentials. Please try again.';
          console.error('Login error:', error);
        }
      });
    }
  }

  onSwitchToRegister(): void {
    this.switchToRegister.emit();
  }
}

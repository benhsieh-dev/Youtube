import { Component, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, RegisterRequest } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  @Output() switchToLogin = new EventEmitter<void>();
  
  registerForm: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.error = '';
      
      const userData: RegisterRequest = this.registerForm.value;
      
      this.authService.register(userData).subscribe({
        next: () => {
          this.loading = false;
          alert('Registration successful! Please sign in.');
          this.switchToLogin.emit();
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Registration failed. Please try again.';
          console.error('Registration error:', error);
        }
      });
    }
  }

  onSwitchToLogin(): void {
    this.switchToLogin.emit();
  }
}
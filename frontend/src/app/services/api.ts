import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Api {
  
  // API Gateway URL - replace with your actual API Gateway URL after deployment
  private readonly API_GATEWAY_URL = 'https://your-api-gateway-id.execute-api.us-east-1.amazonaws.com/prod';
  
  // Fallback to EC2 Spring Boot API for video operations
  private readonly EC2_API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Auth endpoints (API Gateway + Lambda)
  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.API_GATEWAY_URL}/auth/register`, {
      username,
      email, 
      password
    });
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.API_GATEWAY_URL}/auth/login`, {
      username,
      password
    });
  }

  checkUsername(username: string): Observable<any> {
    return this.http.get(`${this.API_GATEWAY_URL}/auth/check?username=${username}`);
  }

  // User endpoints (API Gateway + Lambda)
  getCurrentUserProfile(userId: number, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get(`${this.API_GATEWAY_URL}/users/profile?userId=${userId}`, { headers });
  }

  getUserProfile(username: string): Observable<any> {
    return this.http.get(`${this.API_GATEWAY_URL}/users/${username}`);
  }

  updateUserProfile(userId: number, updates: any, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put(`${this.API_GATEWAY_URL}/users/profile?userId=${userId}`, updates, { headers });
  }

  // Video endpoints (EC2 Spring Boot - not migrated to Lambda)
  uploadVideo(videoData: FormData, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post(`${this.EC2_API_URL}/videos/upload`, videoData, { headers });
  }

  getVideos(): Observable<any> {
    return this.http.get(`${this.EC2_API_URL}/videos`);
  }

  getVideo(videoId: number): Observable<any> {
    return this.http.get(`${this.EC2_API_URL}/videos/${videoId}`);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { JwtAuthResponse } from '../api/model';
import { Result } from '../api/model/Result';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private LOGIN_URL = 'http://localhost:8080/api/v1/auth/login';
  private tokenKey = 'authToken';
  private ISAUTHORIZED_URL = 'http://localhost:8080/api/v1/auth/is_authorized';
  private REFRESH_URL = 'http://localhost:8080/api/v1/auth/refresh_token';
  private refreshTokenKey = 'refreshToken';

  constructor(private httpClient: HttpClient, private router: Router) { }

  login(username: string, password: string): Observable<Result<JwtAuthResponse>> {
    return this.httpClient.post<Result<JwtAuthResponse>>(this.LOGIN_URL, { username, password }).pipe(
      tap(response => {
        if (response.err == null) {
          this.setToken(response.ok.accessToken);
          this.setRefreshToken(response.ok.refreshToken)
          this.autoRefreshToken();
        }
      })
    )
  }

  isAuthorized(role: string): Observable<Result<boolean>> {
    return this.httpClient.get<Result<boolean>>(this.ISAUTHORIZED_URL, {
      headers: {
        "Authorization": `Bearer ${this.getToken()}`
      },
      params: {
        role
      }
    }).pipe(tap(response => {
      if (response.err == null) {
        return response.ok
      } else {
        return false
      }
    }));
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(this.tokenKey);
    } else {
      return null;
    }
  }

  private setRefreshToken(token: string): void {
    localStorage.setItem(this.refreshTokenKey, token);
  }

  private getRefreshToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(this.refreshTokenKey);
    } else {
      return null;
    }
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken()
    return this.httpClient.post<Result<JwtAuthResponse>>(this.REFRESH_URL, { refreshToken }).pipe(
      tap(response => {
        if (response.err == null) {
          this.setToken(response.ok.accessToken);
          this.setRefreshToken(response.ok.refreshToken)
          this.autoRefreshToken()
        }
      })
    )
  }

  autoRefreshToken(): void {
    const token = this.getToken();
    if (!token) {
      return;
    }
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000;

    const timeout = exp - Date.now() - (60 * 1000);

    setTimeout(() => {
      this.refreshToken().subscribe()
    }, timeout);

  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000;
    return Date.now() < exp;
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.router.navigate(['/login']);
  }
}

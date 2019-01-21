import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

type AuthResponse = {
    message?: string
    token?: string
}

type User = {
    username: string
    registeredTimestamp: number
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(
        private http: HttpClient
    ) {
        window["auth"] = this
    }

    private _token: string
    private _user: User

    get token(): string {
        return this._token ? this._token : (this._token = localStorage.getItem("JWT"))
    }

    set token(token: string) {
        if (!token) {
            localStorage.removeItem("JWT")
            this._token = null
        } else localStorage.setItem("JWT", (this._token = token))
    }

    private requestingUser = false

    get user(): User {
        if (!this._user && this.token && !this.requestingUser) {
            this.requestingUser = true
            this.authRequest<User>("get", "me")
                .then(me => this._user = me)
                .catch(e => { console.log(e); this.token = null })
                .finally(() => this.requestingUser = false)
        }
        return this._user
    }

    async loginOrRegister(username: string, password: string, register?: boolean) {
        try {
            let res = await this.authRequest<AuthResponse>(
                "post", register ? "register" : "login",
                { username, password }
            )
            this.token = res.token

        } catch (res) {
            alert(res.error.message)
        }
    }

    authRequest<T>(method: "post" | "get", path: string, body?: any): Promise<T> {

        let headers = {}
        if (this.token)
            headers["Authorization"] = "Bearer " + this.token

        if (method == "post")
            return this.http.post<T>(environment.apiUrl + path, body, { headers }).toPromise()
        if (method == "get")
            return this.http.get<T>(environment.apiUrl + path, { headers }).toPromise()
    }

}

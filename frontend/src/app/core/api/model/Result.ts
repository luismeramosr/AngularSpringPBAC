import { Error } from "./Error";

export type Result<T> = {
  ok: T,
  err: Error
}

export type Error = {
  message?: string;
  cause?: string;
  /** @enum {string} */
  errorKind?: "RepositoryError" | "ServiceError" | "SpecificationError" | "ControllerError" | "NoUsernameInTokenError" | "ExpiredTokenError" | "InvalidTokenError" | "TokenNotFoundError";
}

const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:8080';

export async function authServiceFetch(
  path: string,
  options: RequestInit = {}
): Promise<Response> {
  const url = `${AUTH_SERVICE_URL}${path}`;
  return fetch(url, {
    cache: 'no-store',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
}

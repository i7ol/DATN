export function parseBlobJson<T>(body: any): Promise<T> {
  if (body instanceof Blob) {
    return body.text().then((text) => JSON.parse(text) as T);
  }
  return Promise.resolve(body as T);
}

export interface Store {
  template: string;
  name: string;
  address: string;
  logo: string;
  email: string;
}

export interface JsonFile {
  stores: Store[];
}

export interface UploadResponse {
  message: string;
  success: boolean;
} 
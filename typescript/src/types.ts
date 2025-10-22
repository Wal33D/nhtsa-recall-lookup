/**
 * NHTSA Recall Record
 */
export interface RecallRecord {
  // Campaign identification
  nhtsaCampaignNumber?: string;
  nhtsaActionNumber?: string;
  mfrRecallNumber?: string;

  // Vehicle information
  manufacturer?: string;
  make?: string;
  model?: string;
  modelYear?: string;

  // Recall details
  component?: string;
  summary?: string;
  consequence?: string;
  remedy?: string;
  notes?: string;

  // Dates
  reportReceivedDate?: string;

  // Safety flags
  parkIt?: boolean;
  parkOutside?: boolean;
  overTheAirUpdate?: boolean;

  // Additional fields
  [key: string]: string | number | boolean | undefined;
}

/**
 * NHTSA API Response
 */
export interface NHTSARecallResponse {
  Count: number;
  Message: string;
  Results: Array<Record<string, string | null>>;
}

/**
 * Recall Lookup Options
 */
export interface RecallLookupOptions {
  /**
   * Cache duration in milliseconds (default: 3600000 = 1 hour)
   */
  cacheDuration?: number;

  /**
   * API base URL (default: NHTSA API)
   */
  apiBaseUrl?: string;

  /**
   * Timeout for API requests in milliseconds (default: 10000)
   */
  timeout?: number;
}

/**
 * Recall search criteria
 */
export interface RecallSearchCriteria {
  make: string;
  model: string;
  modelYear?: string;
}

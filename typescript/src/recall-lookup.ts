import axios, { AxiosInstance } from 'axios';
import { RecallRecord, NHTSARecallResponse, RecallLookupOptions } from './types';

/**
 * NHTSA Recall Lookup Service
 */
export class RecallLookup {
  private readonly apiClient: AxiosInstance;
  private readonly cache: Map<string, { data: RecallRecord[]; timestamp: number }>;
  private readonly cacheDuration: number;

  constructor(options: RecallLookupOptions = {}) {
    const baseURL = options.apiBaseUrl || 'https://api.nhtsa.gov/recalls';
    const timeout = options.timeout || 10000;
    this.cacheDuration = options.cacheDuration || 3600000; // 1 hour default

    this.apiClient = axios.create({
      baseURL,
      timeout,
      headers: {
        'Accept': 'application/json',
        'User-Agent': 'nhtsa-recall-lookup-ts/1.0.0'
      }
    });

    this.cache = new Map();
  }

  /**
   * Get recalls for a vehicle by make, model, and optional year
   */
  async getRecalls(make: string, model: string, modelYear?: string): Promise<RecallRecord[]> {
    if (!make || !model) {
      throw new Error('Make and model are required');
    }

    // Check cache
    const cacheKey = `${make}:${model}:${modelYear || 'all'}`.toLowerCase();
    const cached = this.getFromCache(cacheKey);
    if (cached) {
      return cached;
    }

    // Build query parameters
    const params: Record<string, string> = {
      make,
      model
    };

    if (modelYear) {
      params.modelYear = modelYear;
    }

    try {
      const response = await this.apiClient.get<NHTSARecallResponse>('/vehicle/v1/recalls/bymodel', {
        params
      });

      const recalls = this.parseResponse(response.data);

      // Cache result
      this.cache.set(cacheKey, {
        data: recalls,
        timestamp: Date.now()
      });

      return recalls;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(`NHTSA API error: ${error.message}`);
      }
      throw error;
    }
  }

  /**
   * Get recall by campaign number
   */
  async getRecallByCampaignNumber(campaignNumber: string): Promise<RecallRecord[]> {
    if (!campaignNumber) {
      throw new Error('Campaign number is required');
    }

    // Check cache
    const cacheKey = `campaign:${campaignNumber}`.toLowerCase();
    const cached = this.getFromCache(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiClient.get<NHTSARecallResponse>('/vehicle/v1/recalls/bycampaignnumber', {
        params: { campaignNumber }
      });

      const recalls = this.parseResponse(response.data);

      // Cache result
      this.cache.set(cacheKey, {
        data: recalls,
        timestamp: Date.now()
      });

      return recalls;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(`NHTSA API error: ${error.message}`);
      }
      throw error;
    }
  }

  /**
   * Parse NHTSA API response
   */
  private parseResponse(response: NHTSARecallResponse): RecallRecord[] {
    if (!response.Results || response.Results.length === 0) {
      return [];
    }

    return response.Results.map(result => this.parseRecallRecord(result));
  }

  /**
   * Parse individual recall record
   */
  private parseRecallRecord(result: Record<string, string | null>): RecallRecord {
    const record: RecallRecord = {};

    // Map fields
    const fieldMapping: Record<string, keyof RecallRecord> = {
      'NHTSACampaignNumber': 'nhtsaCampaignNumber',
      'NHTSAActionNumber': 'nhtsaActionNumber',
      'Manufacturer': 'manufacturer',
      'Make': 'make',
      'Model': 'model',
      'ModelYear': 'modelYear',
      'Component': 'component',
      'Summary': 'summary',
      'Consequence': 'consequence',
      'Remedy': 'remedy',
      'Notes': 'notes',
      'ReportReceivedDate': 'reportReceivedDate',
      'MfrRecallNumber': 'mfrRecallNumber'
    };

    for (const [apiField, recordField] of Object.entries(fieldMapping)) {
      const value = result[apiField];
      if (value !== null && value !== undefined && value !== '') {
        record[recordField] = value;
      }
    }

    // Parse boolean fields
    record.parkIt = this.parseBoolean(result['ParkIt']);
    record.parkOutside = this.parseBoolean(result['ParkOutSide'] || result['ParkOutside']);
    record.overTheAirUpdate = this.parseBoolean(result['OverTheAirUpdate'] || result['overTheAirUpdateYn']);

    return record;
  }

  /**
   * Parse boolean value from NHTSA API
   */
  private parseBoolean(value: string | null | undefined): boolean | undefined {
    if (!value) return undefined;
    const normalized = value.toLowerCase().trim();
    if (normalized === 'yes' || normalized === 'y' || normalized === '1' || normalized === 'true') {
      return true;
    }
    if (normalized === 'no' || normalized === 'n' || normalized === '0' || normalized === 'false') {
      return false;
    }
    return undefined;
  }

  /**
   * Get from cache if not expired
   */
  private getFromCache(key: string): RecallRecord[] | null {
    const cached = this.cache.get(key);
    if (!cached) return null;

    const age = Date.now() - cached.timestamp;
    if (age > this.cacheDuration) {
      this.cache.delete(key);
      return null;
    }

    return cached.data;
  }

  /**
   * Clear cache
   */
  clearCache(): void {
    this.cache.clear();
  }

  /**
   * Check if query is cached
   */
  isCached(make: string, model: string, modelYear?: string): boolean {
    const cacheKey = `${make}:${model}:${modelYear || 'all'}`.toLowerCase();
    const cached = this.cache.get(cacheKey);
    if (!cached) return false;

    const age = Date.now() - cached.timestamp;
    return age <= this.cacheDuration;
  }

  /**
   * Get cache size
   */
  getCacheSize(): number {
    return this.cache.size;
  }

  /**
   * Check if recall is critical (requires immediate attention)
   */
  static isCriticalRecall(recall: RecallRecord): boolean {
    return recall.parkIt === true || recall.parkOutside === true;
  }

  /**
   * Filter recalls by critical status
   */
  static filterCriticalRecalls(recalls: RecallRecord[]): RecallRecord[] {
    return recalls.filter(recall => RecallLookup.isCriticalRecall(recall));
  }

  /**
   * Count critical recalls
   */
  static countCriticalRecalls(recalls: RecallRecord[]): number {
    return recalls.filter(recall => RecallLookup.isCriticalRecall(recall)).length;
  }

  /**
   * Filter recalls by component
   */
  static filterByComponent(recalls: RecallRecord[], component: string): RecallRecord[] {
    const search = component.toLowerCase();
    return recalls.filter(recall =>
      recall.component?.toLowerCase().includes(search)
    );
  }

  /**
   * Group recalls by year
   */
  static groupByYear(recalls: RecallRecord[]): Map<string, RecallRecord[]> {
    const groups = new Map<string, RecallRecord[]>();

    for (const recall of recalls) {
      const year = recall.modelYear || 'Unknown';
      if (!groups.has(year)) {
        groups.set(year, []);
      }
      groups.get(year)!.push(recall);
    }

    return groups;
  }

  /**
   * Sort recalls by date (newest first)
   */
  static sortByDate(recalls: RecallRecord[]): RecallRecord[] {
    return [...recalls].sort((a, b) => {
      if (!a.reportReceivedDate) return 1;
      if (!b.reportReceivedDate) return -1;
      return b.reportReceivedDate.localeCompare(a.reportReceivedDate);
    });
  }
}

import { RecallLookup, RecallRecord } from '../src';

describe('RecallLookup', () => {
  let lookup: RecallLookup;

  beforeEach(() => {
    lookup = new RecallLookup();
  });

  describe('isCriticalRecall', () => {
    it('should identify critical recalls with parkIt', () => {
      const recall: RecallRecord = { parkIt: true };
      expect(RecallLookup.isCriticalRecall(recall)).toBe(true);
    });

    it('should identify critical recalls with parkOutside', () => {
      const recall: RecallRecord = { parkOutside: true };
      expect(RecallLookup.isCriticalRecall(recall)).toBe(true);
    });

    it('should not identify non-critical recalls', () => {
      const recall: RecallRecord = { parkIt: false, parkOutside: false };
      expect(RecallLookup.isCriticalRecall(recall)).toBe(false);
    });
  });

  describe('filterCriticalRecalls', () => {
    it('should filter critical recalls', () => {
      const recalls: RecallRecord[] = [
        { nhtsaCampaignNumber: '1', parkIt: true },
        { nhtsaCampaignNumber: '2', parkIt: false },
        { nhtsaCampaignNumber: '3', parkOutside: true },
      ];

      const critical = RecallLookup.filterCriticalRecalls(recalls);
      expect(critical).toHaveLength(2);
      expect(critical[0].nhtsaCampaignNumber).toBe('1');
      expect(critical[1].nhtsaCampaignNumber).toBe('3');
    });
  });

  describe('countCriticalRecalls', () => {
    it('should count critical recalls', () => {
      const recalls: RecallRecord[] = [
        { parkIt: true },
        { parkIt: false },
        { parkOutside: true },
      ];

      expect(RecallLookup.countCriticalRecalls(recalls)).toBe(2);
    });
  });

  describe('filterByComponent', () => {
    it('should filter by component', () => {
      const recalls: RecallRecord[] = [
        { component: 'ENGINE' },
        { component: 'BRAKES' },
        { component: 'ENGINE:COOLING' },
      ];

      const engineRecalls = RecallLookup.filterByComponent(recalls, 'engine');
      expect(engineRecalls).toHaveLength(2);
    });

    it('should be case insensitive', () => {
      const recalls: RecallRecord[] = [
        { component: 'ENGINE' },
      ];

      const results = RecallLookup.filterByComponent(recalls, 'Engine');
      expect(results).toHaveLength(1);
    });
  });

  describe('groupByYear', () => {
    it('should group recalls by year', () => {
      const recalls: RecallRecord[] = [
        { modelYear: '2020', nhtsaCampaignNumber: '1' },
        { modelYear: '2020', nhtsaCampaignNumber: '2' },
        { modelYear: '2021', nhtsaCampaignNumber: '3' },
      ];

      const groups = RecallLookup.groupByYear(recalls);
      expect(groups.size).toBe(2);
      expect(groups.get('2020')).toHaveLength(2);
      expect(groups.get('2021')).toHaveLength(1);
    });
  });

  describe('sortByDate', () => {
    it('should sort recalls by date (newest first)', () => {
      const recalls: RecallRecord[] = [
        { reportReceivedDate: '01/01/2020' },
        { reportReceivedDate: '01/01/2022' },
        { reportReceivedDate: '01/01/2021' },
      ];

      const sorted = RecallLookup.sortByDate(recalls);
      expect(sorted[0].reportReceivedDate).toBe('01/01/2022');
      expect(sorted[1].reportReceivedDate).toBe('01/01/2021');
      expect(sorted[2].reportReceivedDate).toBe('01/01/2020');
    });
  });

  describe('cache', () => {
    it('should check cache status', () => {
      expect(lookup.isCached('Honda', 'CR-V', '2019')).toBe(false);
    });

    it('should clear cache', () => {
      lookup.clearCache();
      expect(lookup.getCacheSize()).toBe(0);
    });

    it('should report cache size', () => {
      expect(lookup.getCacheSize()).toBe(0);
    });
  });
});

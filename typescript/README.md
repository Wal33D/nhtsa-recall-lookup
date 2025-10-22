# NHTSA Recall Lookup - TypeScript/JavaScript

Vehicle recall campaign lookup for Node.js and browsers using NHTSA API.

## Installation

```bash
npm install @wal33d/nhtsa-recall-lookup
```

## Quick Start

```typescript
import { RecallLookup } from '@wal33d/nhtsa-recall-lookup';

const lookup = new RecallLookup();

// Get recalls by VIN (easiest method)
const recalls = await lookup.getRecallsByVIN('1HGCM82633A123456');
console.log(`Found ${recalls.length} recalls`);

// Or get recalls by make/model/year
const recalls2 = await lookup.getRecalls('Honda', 'CR-V', '2019');

// Filter critical recalls
const critical = RecallLookup.filterCriticalRecalls(recalls);
console.log(`${critical.length} critical recalls`);

// Get recall by campaign number
const campaign = await lookup.getRecallByCampaignNumber('20V123000');
```

## Features

- ✅ **Direct VIN lookup** - Pass VIN, get recalls
- ✅ Search by make/model/year
- ✅ Search by campaign number
- ✅ Critical recall detection (parkIt, parkOutside)
- ✅ Built-in caching (1 hour default)
- ✅ TypeScript support
- ✅ Helper methods (filter, group, sort)

## API

### RecallLookup

```typescript
const lookup = new RecallLookup(options);
```

**Methods:**
- `getRecallsByVIN(vin)` - Get recalls by VIN (recommended)
- `getRecalls(make, model, year?)` - Get recalls for vehicle
- `getRecallByCampaignNumber(number)` - Get specific recall
- `clearCache()` - Clear cache
- `isCached(make, model, year?)` - Check if cached

**Static Methods:**
- `isCriticalRecall(recall)` - Check if critical
- `filterCriticalRecalls(recalls)` - Filter critical
- `countCriticalRecalls(recalls)` - Count critical
- `filterByComponent(recalls, component)` - Filter by component
- `groupByYear(recalls)` - Group by year
- `sortByDate(recalls)` - Sort by date

## License

MIT © [Wal33D](https://github.com/Wal33D)

/**
 * NHTSA Recall Lookup - TypeScript Examples
 */

import { RecallLookup } from '../src';

async function example1_basicLookup() {
  console.log('=== Example 1: Basic Recall Lookup ===\n');

  const lookup = new RecallLookup();

  try {
    const recalls = await lookup.getRecalls('Honda', 'CR-V', '2019');
    console.log(`Found ${recalls.length} recalls for 2019 Honda CR-V`);

    if (recalls.length > 0) {
      const first = recalls[0];
      console.log(`\nFirst recall:`);
      console.log(`  Campaign: ${first.nhtsaCampaignNumber}`);
      console.log(`  Component: ${first.component}`);
      console.log(`  Summary: ${first.summary || 'N/A'}`);
    }
  } catch (error) {
    console.error('Error:', error);
  }
  console.log();
}

async function example2_criticalRecalls() {
  console.log('=== Example 2: Filter Critical Recalls ===\n');

  const lookup = new RecallLookup();

  try {
    const recalls = await lookup.getRecalls('Tesla', 'Model 3', '2020');
    const critical = RecallLookup.filterCriticalRecalls(recalls);

    console.log(`Total recalls: ${recalls.length}`);
    console.log(`Critical recalls: ${critical.length}`);

    if (critical.length > 0) {
      console.log('\n⚠️  CRITICAL RECALLS:');
      for (const recall of critical) {
        console.log(`  ${recall.nhtsaCampaignNumber}: ${recall.component}`);
      }
    }
  } catch (error) {
    console.error('Error:', error);
  }
  console.log();
}

async function example3_groupByYear() {
  console.log('=== Example 3: Group Recalls by Year ===\n');

  const lookup = new RecallLookup();

  try {
    const recalls = await lookup.getRecalls('Ford', 'F-150');
    const groups = RecallLookup.groupByYear(recalls);

    console.log('Recalls by Year:');
    for (const [year, yearRecalls] of groups.entries()) {
      console.log(`  ${year}: ${yearRecalls.length} recalls`);
    }
  } catch (error) {
    console.error('Error:', error);
  }
  console.log();
}

async function runAllExamples() {
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║       NHTSA Recall Lookup - TypeScript Examples              ║');
  console.log('╚══════════════════════════════════════════════════════════════╝\n');

  await example1_basicLookup();
  await example2_criticalRecalls();
  await example3_groupByYear();

  console.log('✅ All examples completed!');
}

if (require.main === module) {
  runAllExamples();
}

export { example1_basicLookup, example2_criticalRecalls, example3_groupByYear };

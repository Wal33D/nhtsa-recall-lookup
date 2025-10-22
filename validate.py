#!/usr/bin/env python3
"""
Validation script for NHTSA Recall Lookup
Tests that the library is working correctly
"""

import sys
import os
import time

# Add python directory to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'python'))

def test_import():
    """Test that the library can be imported"""
    try:
        from nhtsa_recall_lookup import NHTSARecallLookup, RecallRecord
        print("✓ Import successful")
        return True
    except ImportError as e:
        print(f"✗ Import failed: {e}")
        return False

def test_basic_lookup():
    """Test basic recall lookup"""
    from nhtsa_recall_lookup import NHTSARecallLookup

    print("\nTesting recall lookup for 2019 Honda CR-V...")
    lookup = NHTSARecallLookup()

    try:
        recalls = lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")

        if isinstance(recalls, list):
            print(f"✓ API call successful - Found {len(recalls)} recalls")

            if recalls:
                recall = recalls[0]
                if hasattr(recall, 'nhtsa_campaign_number'):
                    print(f"✓ Data structure correct")
                    print(f"  Sample: Campaign #{recall.nhtsa_campaign_number}")
                    if recall.component:
                        print(f"  Component: {recall.component[:50]}...")
                else:
                    print("⚠ Data structure unexpected")
            return True
        else:
            print(f"✗ Unexpected response type: {type(recalls)}")
            return False

    except Exception as e:
        print(f"✗ API call failed: {e}")
        return False

def test_critical_detection():
    """Test critical safety detection"""
    from nhtsa_recall_lookup import RecallRecord

    print("\nTesting critical safety detection...")

    # Create test recall
    recall = RecallRecord(
        nhtsa_campaign_number="TEST001",
        park_outside=True,
        park_it=False
    )

    if recall.is_critical_safety():
        print("✓ Critical safety detection working")
        return True
    else:
        print("✗ Critical safety detection failed")
        return False

def test_filtering():
    """Test filtering capabilities"""
    from nhtsa_recall_lookup import NHTSARecallLookup, RecallRecord

    print("\nTesting filtering capabilities...")

    lookup = NHTSARecallLookup()

    # Create test recalls
    recalls = [
        RecallRecord(
            nhtsa_campaign_number="TEST001",
            park_it=True,
            component="AIRBAGS"
        ),
        RecallRecord(
            nhtsa_campaign_number="TEST002",
            park_it=False,
            component="ENGINE"
        ),
    ]

    # Test critical filter
    critical = lookup.filter_critical_recalls(recalls)
    if len(critical) == 1 and critical[0].nhtsa_campaign_number == "TEST001":
        print("✓ Critical filtering works")
    else:
        print("✗ Critical filtering failed")
        return False

    # Test component filter
    airbag_recalls = lookup.filter_by_component(recalls, "AIRBAG")
    if len(airbag_recalls) == 1:
        print("✓ Component filtering works")
        return True
    else:
        print("✗ Component filtering failed")
        return False

def main():
    """Run all validation tests"""
    print("=" * 50)
    print("NHTSA Recall Lookup Validation")
    print("=" * 50)

    tests = [
        ("Import Test", test_import),
        ("Basic Lookup Test", test_basic_lookup),
        ("Critical Detection Test", test_critical_detection),
        ("Filtering Test", test_filtering)
    ]

    results = []

    for name, test_func in tests:
        print(f"\n{name}")
        print("-" * 30)
        success = test_func()
        results.append((name, success))
        time.sleep(0.5)  # Small delay between tests

    # Summary
    print("\n" + "=" * 50)
    print("VALIDATION SUMMARY")
    print("=" * 50)

    passed = sum(1 for _, success in results if success)
    total = len(results)

    for name, success in results:
        status = "✓ PASS" if success else "✗ FAIL"
        print(f"{name:30} {status}")

    print("-" * 50)
    print(f"Result: {passed}/{total} tests passed")

    if passed == total:
        print("\n🎉 All tests passed! The library is working correctly.")
        return 0
    else:
        print(f"\n⚠️  {total - passed} test(s) failed. Please check the errors above.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
#!/usr/bin/env python3
"""
Example Python application demonstrating NHTSA Recall Lookup
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from python.nhtsa_recall_lookup import NHTSARecallLookup, RecallRecord
from typing import List


def display_recall(recall: RecallRecord, index: int):
    """Display a single recall with formatting"""
    print(f"\nRecall #{index}:")
    print(f"  Campaign: {recall.nhtsa_campaign_number}")
    print(f"  Component: {recall.component}")

    if recall.summary:
        summary = recall.summary[:150] + "..." if len(recall.summary) > 150 else recall.summary
        print(f"  Summary: {summary}")

    if recall.is_critical_safety():
        print("  ⚠️  CRITICAL SAFETY RECALL")
        if recall.park_it:
            print("     DO NOT DRIVE THIS VEHICLE")
        if recall.park_outside:
            print("     PARK OUTSIDE - FIRE RISK")

    if recall.is_over_the_air():
        print("  ✓ Can be fixed via over-the-air update")


def interactive_lookup():
    """Interactive recall lookup"""
    lookup = NHTSARecallLookup()

    print("NHTSA Recall Lookup Example")
    print("============================\n")

    make = input("Enter vehicle make (e.g., Honda): ").strip()
    model = input("Enter vehicle model (e.g., Accord): ").strip()
    year = input("Enter model year (optional, press Enter to skip): ").strip()

    print("\nSearching for recalls...\n")

    recalls = lookup.get_recalls_for_vehicle(
        make, model, year if year else None
    )

    if not recalls:
        print("No recalls found for this vehicle.")
    else:
        print(f"Found {len(recalls)} recalls:")

        # Display first 5 recalls in detail
        for i, recall in enumerate(recalls[:5], 1):
            display_recall(recall, i)

        if len(recalls) > 5:
            print(f"\n... and {len(recalls) - 5} more recalls")

        # Summary statistics
        critical_recalls = lookup.filter_critical_recalls(recalls)
        ota_recalls = [r for r in recalls if r.is_over_the_air()]

        print("\n" + "=" * 50)
        print("Summary:")
        print(f"  Total recalls: {len(recalls)}")
        if critical_recalls:
            print(f"  ⚠️  Critical safety recalls: {len(critical_recalls)}")
        if ota_recalls:
            print(f"  ✓ OTA fixable recalls: {len(ota_recalls)}")


def batch_lookup_example():
    """Demonstrate batch vehicle recall lookups"""
    lookup = NHTSARecallLookup()

    print("\n\nBatch Lookup Example")
    print("====================")

    vehicles = [
        ("Honda", "CR-V", "2019"),
        ("Toyota", "Camry", "2020"),
        ("Ford", "F-150", "2021"),
        ("Tesla", "Model 3", "2022"),
        ("Chevrolet", "Silverado", "2020")
    ]

    results = []

    for make, model, year in vehicles:
        recalls = lookup.get_recalls_for_vehicle(make, model, year)
        critical = lookup.filter_critical_recalls(recalls)

        vehicle_info = f"{year} {make} {model}"

        if recalls:
            status = f"{len(recalls)} recalls"
            if critical:
                status += f" (⚠️  {len(critical)} critical)"
            print(f"{vehicle_info}: {status}")
        else:
            print(f"{vehicle_info}: ✓ No recalls")

        results.append((vehicle_info, recalls))

    # Find vehicle with most recalls
    if results:
        most_recalls = max(results, key=lambda x: len(x[1]))
        if most_recalls[1]:
            print(f"\nMost recalls: {most_recalls[0]} with {len(most_recalls[1])} recalls")


def component_analysis_example():
    """Analyze recalls by component"""
    lookup = NHTSARecallLookup()

    print("\n\nComponent Analysis Example")
    print("==========================")

    # Get all recalls for a popular vehicle
    recalls = lookup.get_recalls_for_vehicle("Honda", "Accord")

    if recalls:
        print(f"Analyzing {len(recalls)} Honda Accord recalls...\n")

        # Group by component keyword
        component_keywords = ["AIR BAG", "ENGINE", "BRAKE", "FUEL", "ELECTRICAL", "SEAT"]
        component_counts = {}

        for keyword in component_keywords:
            filtered = lookup.filter_by_component(recalls, keyword)
            if filtered:
                component_counts[keyword] = len(filtered)

        # Display results
        if component_counts:
            print("Recalls by component type:")
            for component, count in sorted(component_counts.items(), key=lambda x: x[1], reverse=True):
                bar = "█" * (count * 2)  # Visual bar chart
                print(f"  {component:12} {bar} {count}")

        # Group by year
        grouped_by_year = lookup.group_by_year(recalls)
        print(f"\nRecalls by model year (showing top 5):")
        for year in sorted(grouped_by_year.keys(), reverse=True)[:5]:
            count = len(grouped_by_year[year])
            print(f"  {year}: {count} recalls")


def campaign_lookup_example():
    """Look up specific recall by campaign number"""
    lookup = NHTSARecallLookup()

    print("\n\nCampaign Lookup Example")
    print("=======================")

    # Example campaign number (Honda CR-V recall)
    campaign_number = "19V182000"

    print(f"Looking up campaign #{campaign_number}...\n")

    recall = lookup.get_recall_by_campaign_number(campaign_number)

    if recall:
        print(f"Found recall:")
        print(f"  Make/Model: {recall.make} {recall.model} {recall.model_year}")
        print(f"  Component: {recall.component}")
        print(f"  Summary: {recall.summary}")
        print(f"  Consequence: {recall.consequence}")
        print(f"  Remedy: {recall.remedy}")

        if recall.is_critical_safety():
            print("\n⚠️  This is a CRITICAL SAFETY recall")
    else:
        print("Campaign not found")


def main():
    """Run all examples"""
    # Interactive lookup
    interactive_lookup()

    # Run other examples
    batch_lookup_example()
    component_analysis_example()
    campaign_lookup_example()

    # Clear cache
    lookup = NHTSARecallLookup()
    lookup.clear_cache()
    print("\n✓ Cache cleared")


if __name__ == "__main__":
    main()
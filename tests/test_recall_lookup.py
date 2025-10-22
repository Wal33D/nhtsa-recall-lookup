#!/usr/bin/env python3
"""Unit tests for NHTSA Recall Lookup"""

import json
import unittest
from typing import Dict, List
from unittest.mock import patch
import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from python.nhtsa_recall_lookup import NHTSARecallLookup, RecallRecord


class DummyResponse:
    """Mock urllib response"""

    def __init__(self, payload: Dict):
        self._payload = payload

    def read(self) -> bytes:
        return json.dumps(self._payload).encode("utf-8")

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        return False


def sample_recall_payload() -> Dict:
    """Sample recall API response"""
    return {
        "Count": 2,
        "Message": "Results returned successfully",
        "results": [
            {
                "Manufacturer": "Honda (American Honda Motor Co.)",
                "NHTSACampaignNumber": "19V182000",
                "parkIt": False,
                "parkOutSide": True,
                "overTheAirUpdate": False,
                "ReportReceivedDate": "06/03/2019",
                "Component": "FUEL SYSTEM, GASOLINE:STORAGE:TANK ASSEMBLY",
                "Summary": "Fuel pump may fail causing engine stall",
                "Consequence": "Engine stall increases crash risk",
                "Remedy": "Replace fuel pump module",
                "Notes": "Dealers will replace free of charge",
                "ModelYear": "2019",
                "Make": "HONDA",
                "Model": "CR-V",
            },
            {
                "Manufacturer": "Honda (American Honda Motor Co.)",
                "NHTSACampaignNumber": "20V123000",
                "parkIt": False,
                "parkOutSide": False,
                "overTheAirUpdate": True,
                "ReportReceivedDate": "03/15/2020",
                "Component": "ELECTRICAL SYSTEM:SOFTWARE",
                "Summary": "Software issue may cause display malfunction",
                "Consequence": "Display issues may distract driver",
                "Remedy": "Software update",
                "Notes": "Over-the-air update available",
                "ModelYear": "2019",
                "Make": "HONDA",
                "Model": "CR-V",
            }
        ],
    }


class TestRecallLookup(unittest.TestCase):
    """Test NHTSA Recall Lookup functionality"""

    def setUp(self):
        """Set up test fixtures"""
        self.lookup = NHTSARecallLookup()

    def test_recall_record_critical_safety(self):
        """Test critical safety detection"""
        # Normal recall
        normal_recall = RecallRecord(
            nhtsa_campaign_number="TEST001",
            park_it=False,
            park_outside=False
        )
        self.assertFalse(normal_recall.is_critical_safety())

        # Park it recall
        park_it_recall = RecallRecord(
            nhtsa_campaign_number="TEST002",
            park_it=True,
            park_outside=False
        )
        self.assertTrue(park_it_recall.is_critical_safety())

        # Park outside recall
        park_outside_recall = RecallRecord(
            nhtsa_campaign_number="TEST003",
            park_it=False,
            park_outside=True
        )
        self.assertTrue(park_outside_recall.is_critical_safety())

    def test_recall_record_ota(self):
        """Test OTA update detection"""
        # Non-OTA recall
        non_ota = RecallRecord(
            nhtsa_campaign_number="TEST001",
            over_the_air_update=False
        )
        self.assertFalse(non_ota.is_over_the_air())

        # OTA recall
        ota = RecallRecord(
            nhtsa_campaign_number="TEST002",
            over_the_air_update=True
        )
        self.assertTrue(ota.is_over_the_air())

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_get_recalls_for_vehicle(self, mock_urlopen):
        """Test vehicle recall lookup"""
        mock_urlopen.return_value = DummyResponse(sample_recall_payload())

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")

        self.assertEqual(len(recalls), 2)

        # Check first recall
        recall1 = recalls[0]
        self.assertEqual(recall1.nhtsa_campaign_number, "19V182000")
        self.assertEqual(recall1.make, "HONDA")
        self.assertEqual(recall1.model, "CR-V")
        self.assertEqual(recall1.model_year, "2019")
        self.assertFalse(recall1.park_it)
        self.assertTrue(recall1.park_outside)
        self.assertFalse(recall1.over_the_air_update)
        self.assertTrue(recall1.is_critical_safety())

        # Check second recall
        recall2 = recalls[1]
        self.assertEqual(recall2.nhtsa_campaign_number, "20V123000")
        self.assertTrue(recall2.over_the_air_update)
        self.assertFalse(recall2.is_critical_safety())
        self.assertTrue(recall2.is_over_the_air())

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_get_recalls_no_year(self, mock_urlopen):
        """Test recall lookup without year"""
        mock_urlopen.return_value = DummyResponse(sample_recall_payload())

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V")

        self.assertEqual(len(recalls), 2)
        # URL should not have modelYear parameter when year is None
        mock_urlopen.assert_called_once()
        call_args = str(mock_urlopen.call_args)
        self.assertNotIn("modelYear", call_args)

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_filter_critical_recalls(self, mock_urlopen):
        """Test critical recall filtering"""
        mock_urlopen.return_value = DummyResponse(sample_recall_payload())

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")
        critical = self.lookup.filter_critical_recalls(recalls)

        self.assertEqual(len(critical), 1)
        self.assertEqual(critical[0].nhtsa_campaign_number, "19V182000")
        self.assertTrue(critical[0].park_outside)

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_filter_by_component(self, mock_urlopen):
        """Test component filtering"""
        mock_urlopen.return_value = DummyResponse(sample_recall_payload())

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")

        # Filter for FUEL
        fuel_recalls = self.lookup.filter_by_component(recalls, "FUEL")
        self.assertEqual(len(fuel_recalls), 1)
        self.assertEqual(fuel_recalls[0].nhtsa_campaign_number, "19V182000")

        # Filter for SOFTWARE
        software_recalls = self.lookup.filter_by_component(recalls, "SOFTWARE")
        self.assertEqual(len(software_recalls), 1)
        self.assertEqual(software_recalls[0].nhtsa_campaign_number, "20V123000")

        # Filter for non-existent component
        brake_recalls = self.lookup.filter_by_component(recalls, "BRAKE")
        self.assertEqual(len(brake_recalls), 0)

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_group_by_year(self, mock_urlopen):
        """Test grouping by year"""
        # Create payload with multiple years
        payload = {
            "results": [
                {
                    "NHTSACampaignNumber": "TEST001",
                    "ModelYear": "2019",
                    "Make": "Honda",
                    "Model": "CR-V"
                },
                {
                    "NHTSACampaignNumber": "TEST002",
                    "ModelYear": "2019",
                    "Make": "Honda",
                    "Model": "CR-V"
                },
                {
                    "NHTSACampaignNumber": "TEST003",
                    "ModelYear": "2020",
                    "Make": "Honda",
                    "Model": "CR-V"
                }
            ]
        }

        mock_urlopen.return_value = DummyResponse(payload)

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V")
        grouped = self.lookup.group_by_year(recalls)

        self.assertEqual(len(grouped), 2)
        self.assertIn("2019", grouped)
        self.assertIn("2020", grouped)
        self.assertEqual(len(grouped["2019"]), 2)
        self.assertEqual(len(grouped["2020"]), 1)

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_empty_response(self, mock_urlopen):
        """Test handling of empty response"""
        mock_urlopen.return_value = DummyResponse({"results": []})

        recalls = self.lookup.get_recalls_for_vehicle("Unknown", "Model")

        self.assertEqual(len(recalls), 0)

    @patch("python.nhtsa_recall_lookup.urllib.request.urlopen")
    def test_network_error(self, mock_urlopen):
        """Test handling of network errors"""
        mock_urlopen.side_effect = Exception("Network error")

        recalls = self.lookup.get_recalls_for_vehicle("Honda", "CR-V")

        self.assertEqual(len(recalls), 0)

    def test_clean_value(self):
        """Test value cleaning"""
        self.assertEqual(self.lookup._clean_value("Test"), "Test")
        self.assertEqual(self.lookup._clean_value("  Test  "), "Test")
        self.assertIsNone(self.lookup._clean_value(None))
        self.assertIsNone(self.lookup._clean_value(""))
        self.assertIsNone(self.lookup._clean_value("null"))
        self.assertIsNone(self.lookup._clean_value("Not Applicable"))

    def test_parse_bool(self):
        """Test boolean parsing"""
        # True values
        self.assertTrue(self.lookup._parse_bool(True))
        self.assertTrue(self.lookup._parse_bool(1))
        self.assertTrue(self.lookup._parse_bool("Y"))
        self.assertTrue(self.lookup._parse_bool("yes"))
        self.assertTrue(self.lookup._parse_bool("true"))

        # False values
        self.assertFalse(self.lookup._parse_bool(False))
        self.assertFalse(self.lookup._parse_bool(0))
        self.assertFalse(self.lookup._parse_bool("N"))
        self.assertFalse(self.lookup._parse_bool("no"))
        self.assertFalse(self.lookup._parse_bool("false"))

        # None values
        self.assertIsNone(self.lookup._parse_bool(None))
        self.assertIsNone(self.lookup._parse_bool("unknown"))

    def test_cache_clearing(self):
        """Test cache clearing"""
        # This should not raise any errors
        self.lookup.clear_cache()


if __name__ == "__main__":
    unittest.main()
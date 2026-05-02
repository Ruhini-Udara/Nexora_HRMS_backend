# Attendance Device Bridge Readiness

This document prepares the HRMS backend for a future Windows/device bridge that reads real fingerprint device punches and sends them into the existing attendance ingestion API.

Current scope is fingerprint/device-based attendance only. This does not cover manual attendance approvals, overtime, payroll, attendance verification, fingerprint template storage, real-time monitoring, or direct DLL/SDK integration.

## User ID Mapping

The fingerprint device user ID must map to `employee.fingerprintUserId`.

Example:

| Device user ID | HRMS field |
| --- | --- |
| `101` | `employee.fingerprintUserId = 101` |

The bridge should send the device user ID as `terminalUserId` in each punch.

If a punch arrives for a device user ID that has no matching employee `fingerprintUserId`, ingestion can still store the raw punch, but processing will report that no matching employee was found.

Before office testing:

- Ensure each enrolled employee has a numeric `fingerprintUserId`.
- Ensure the value exactly matches the user ID registered on the fingerprint device.
- Do not send fingerprint templates to this backend. Template storage is out of scope.

## Batch Ingestion Endpoint

Future bridge clients should send punches to:

```http
POST /api/attendance/punches/batch
Content-Type: application/json
```

Current local testing does not require an API key. For future production hardening, the bridge should be prepared to send:

```http
X-Device-Api-Key: <device bridge api key>
```

Do not enforce this header until the backend security change is intentionally implemented and coordinated with local testing.

## Expected Payload

```json
{
  "deviceCode": "MOCK-AAS-001",
  "punches": [
    {
      "terminalUserId": 101,
      "punchTime": "2026-05-02T08:30:00",
      "sourceRecordKey": "MOCK-AAS-001:101:20260502083000:IN",
      "rawPayload": "{\"deviceUserId\":101,\"verifyMode\":\"fingerprint\"}"
    }
  ]
}
```

Field notes:

| Field | Required | Notes |
| --- | --- | --- |
| `deviceCode` | Yes | Must match an existing `attendance_device.device_code`. |
| `punches` | Yes | Can be empty, but normal bridge syncs should send collected punches. |
| `terminalUserId` | Yes | Numeric fingerprint device user ID. Maps to `employee.fingerprintUserId`. |
| `punchTime` | Yes | Local date-time in ISO format, for example `2026-05-02T08:30:00`. |
| `sourceRecordKey` | Yes | Stable unique punch key per device. Used for duplicate protection. |
| `rawPayload` | No | Optional raw vendor/device data for diagnostics. |

## sourceRecordKey Format

Use a stable key that uniquely identifies one punch on one device.

Recommended format:

```text
<deviceCode>:<terminalUserId>:<yyyyMMddHHmmss>:<eventType-or-recordId>
```

Examples:

```text
MOCK-AAS-001:101:20260502083000:IN
OFFICE-FP-01:205:20260502174512:OUT
OFFICE-FP-01:205:20260502174512:device-log-987654
```

Rules:

- The same real punch must always produce the same `sourceRecordKey`.
- Retrying a failed sync should resend the same keys so duplicate protection can work.
- If the device SDK exposes a unique log ID, include it in the key.
- If no unique log ID exists, combine `deviceCode`, `terminalUserId`, exact punch timestamp, and event type/status if available.

## Recommended Sync Flow

1. Load bridge configuration: backend URL, `deviceCode`, optional API key, sync interval, and last successful cursor/time.
2. Connect to the device using the vendor SDK or export file. Do not add SDK/DLL code to this backend.
3. Read new punch logs since the previous cursor/time.
4. Convert each device log into the batch payload shape.
5. POST the batch to `/api/attendance/punches/batch`.
6. Store the last sync cursor/time only after the backend accepts the batch.
7. Keep the raw device values in `rawPayload` when possible for diagnostics.
8. Review `/api/attendance/sync-runs` after each office test sync.
9. Trigger `/api/attendance/punches/process` using the existing backend flow when processed daily summaries are needed.

## API Key Readiness

The future bridge should support sending a device API key with each request:

```http
X-Device-Api-Key: <secret>
```

Recommended future backend design:

- Store the expected key in environment/configuration, not in source code.
- Validate the key only for device ingestion routes such as `/api/attendance/punches/batch`.
- Keep JWT behavior unchanged for normal HRMS users.
- Roll out in a way that preserves local mock testing, for example by allowing the key to be disabled in local profiles.

This Step 7 does not implement API-key enforcement.

## Office/Device Testing Checklist

- Create or verify an active `attendance_device` row for the real device.
- Confirm `deviceCode` in bridge config exactly matches the backend device code.
- Enroll one test employee on the device.
- Set that employee's `fingerprintUserId` to the exact numeric device user ID.
- Capture one in punch and one out punch on the physical device.
- Run the bridge sync.
- Confirm `/api/attendance/sync-runs` shows the attempt and counts.
- Confirm duplicate sync retries increase duplicate count instead of inserting copies.
- Run `/api/attendance/punches/process`.
- Confirm `/api/attendance/daily` shows the expected daily summary.
- Test an unknown device user ID and verify diagnostics show the missing `fingerprintUserId` mapping.

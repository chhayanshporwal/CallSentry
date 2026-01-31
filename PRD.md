# Product Requirements Document (PRD)
## SafeGuard - Whitelist Call & SMS Blocker

**Version:** 1.0  
**Date:** January 31, 2026  
**Author:** Development Team  
**Status:** Draft

---

## 1. Executive Summary

### 1.1 Problem Statement
In today's digital age, unwanted calls and messages have become a pervasive problem. Spam calls, telemarketing, scam attempts, and unwanted messages interrupt daily life and pose security risks. Existing solutions typically use a **blacklist approach**, requiring users to manually block numbers after receiving unwanted communication—a reactive strategy.

**SafeGuard** takes a proactive approach by implementing a **whitelist-only system**: only pre-approved contacts can reach the user. All other calls and SMS are automatically blocked, providing maximum protection and peace of mind.

### 1.2 Solution Overview
SafeGuard is an Android application that:
- Maintains a user-defined whitelist of trusted contacts
- Automatically blocks all calls and SMS from numbers not on the whitelist
- Provides emergency override capabilities
- Offers a clean, intuitive interface for managing trusted contacts
- Logs blocked communications for transparency

### 1.3 Target Audience
- **Primary:** Elderly users, children's phones (parental control), high-profile individuals
- **Secondary:** Privacy-conscious users, professionals seeking distraction-free modes

---

## 2. User Personas

### Persona 1: Elderly User - "Protecting Grandma"
| Attribute | Details |
|-----------|---------|
| **Name** | Mrs. Sharma, 68 |
| **Tech Skill** | Basic smartphone user |
| **Pain Point** | Frequently receives scam calls claiming to be from banks |
| **Goal** | Only receive calls from family and known contacts |
| **Key Needs** | Simple UI, easy setup, family can manage remotely |

### Persona 2: Parent - "Child Safety First"
| Attribute | Details |
|-----------|---------|
| **Name** | Rahul, 38, Father of two |
| **Tech Skill** | Moderate |
| **Pain Point** | Worried about unknown people contacting children |
| **Goal** | Restrict children's phone to only approved contacts |
| **Key Needs** | PIN-protected settings, parental dashboard |

### Persona 3: Professional - "Focus Mode"
| Attribute | Details |
|-----------|---------|
| **Name** | Priya, 29, Startup Founder |
| **Tech Skill** | Advanced |
| **Pain Point** | Constant interruptions from spam during work hours |
| **Goal** | Only allow important contacts during work hours |
| **Key Needs** | Scheduled whitelist modes, quick toggle |

---

## 3. User Stories

### Core Features
| ID | User Story | Priority |
|----|-----------|----------|
| US-01 | As a user, I want to add contacts to my whitelist so only they can call/message me | P0 |
| US-02 | As a user, I want incoming calls from non-whitelisted numbers to be automatically rejected | P0 |
| US-03 | As a user, I want SMS from non-whitelisted numbers to be blocked/hidden | P0 |
| US-04 | As a user, I want to import contacts from my phone's contact list easily | P0 |
| US-05 | As a user, I want to see a log of blocked calls/messages | P1 |
| US-06 | As a user, I want to temporarily disable blocking (vacation mode) | P1 |
| US-07 | As a user, I want to set up PIN protection for app settings | P1 |
| US-08 | As a user, I want emergency numbers (100, 101, 112) to always work | P0 |
| US-09 | As a user, I want to receive notifications about blocked attempts | P2 |
| US-10 | As a user, I want to schedule when blocking is active | P2 |

### Advanced Features
| ID | User Story | Priority |
|----|-----------|----------|
| US-11 | As a parent, I want to manage my child's whitelist remotely | P2 |
| US-12 | As a user, I want to backup/restore my whitelist | P1 |
| US-13 | As a user, I want to add numbers manually without adding to contacts | P1 |
| US-14 | As a user, I want to block calls but allow SMS from certain numbers | P3 |

---

## 4. Functional Requirements

### 4.1 Core Functionality

#### FR-01: Whitelist Management
- **FR-01.1:** Users can add contacts from phone's contact book
- **FR-01.2:** Users can manually add phone numbers with optional labels
- **FR-01.3:** Users can remove contacts from whitelist
- **FR-01.4:** Users can search/filter whitelist entries
- **FR-01.5:** Bulk import/export of whitelist (CSV format)

#### FR-02: Call Blocking
- **FR-02.1:** Intercept all incoming calls using Android's CallScreeningService
- **FR-02.2:** Check caller against whitelist in < 100ms
- **FR-02.3:** Reject non-whitelisted calls silently (no ring)
- **FR-02.4:** Allow emergency numbers (configurable list)
- **FR-02.5:** Log all blocked calls with timestamp

#### FR-03: SMS Blocking
- **FR-03.1:** Filter incoming SMS from non-whitelisted numbers
- **FR-03.2:** Move blocked messages to a hidden "Blocked" folder
- **FR-03.3:** Option to auto-delete blocked messages after X days
- **FR-03.4:** Log blocked SMS with timestamp and preview

#### FR-04: User Interface
- **FR-04.1:** Dashboard showing blocking status and recent activity
- **FR-04.2:** Quick toggle to enable/disable blocking
- **FR-04.3:** Settings screen with all configuration options
- **FR-04.4:** Blocked log viewer with filtering options
- **FR-04.5:** Dark mode support

### 4.2 Security Features

#### FR-05: Access Control
- **FR-05.1:** Optional PIN/biometric lock for app access
- **FR-05.2:** PIN required to disable blocking (optional)
- **FR-05.3:** Hide app icon option (for child safety mode)

---

## 5. Non-Functional Requirements

### 5.1 Performance
| Requirement | Target |
|-------------|--------|
| Whitelist lookup time | < 50ms |
| App cold start time | < 2 seconds |
| Memory usage (background) | < 30 MB |
| Battery impact | < 2% daily |

### 5.2 Reliability
| Requirement | Target |
|-------------|--------|
| Blocking accuracy | 100% (no false positives for whitelisted) |
| Service uptime | 99.9% (must survive device restarts) |
| Data persistence | Zero data loss |

### 5.3 Compatibility
| Requirement | Specification |
|-------------|--------------|
| Minimum Android Version | Android 8.0 (API 26) |
| Target Android Version | Android 14 (API 34) |
| Screen sizes | All (phone, tablet) |
| Languages | English, Hindi (initially) |

### 5.4 Usability
- Maximum 3 taps to add a contact
- Maximum 2 taps to enable/disable blocking
- All features accessible without tutorial
- WCAG 2.0 AA accessibility compliance

---

## 6. Success Metrics

### 6.1 Key Performance Indicators (KPIs)

| Metric | Target (3 months) | Target (6 months) |
|--------|------------------|------------------|
| Downloads | 10,000 | 50,000 |
| Daily Active Users (DAU) | 3,000 | 15,000 |
| User Retention (Day 7) | 40% | 50% |
| App Store Rating | 4.0+ | 4.3+ |
| Crash-free rate | 99.5% | 99.9% |

### 6.2 User Satisfaction Metrics
- Net Promoter Score (NPS) > 30
- Customer Satisfaction (CSAT) > 4.0/5.0
- Support ticket volume < 5% of DAU

---

## 7. Out of Scope (v1.0)

The following features are explicitly **not included** in v1.0:
- iOS version
- Cloud-based spam database integration
- AI-powered spam detection
- Call recording
- WhatsApp/Telegram integration
- Web dashboard for remote management
- Paid premium features

---

## 8. Assumptions & Dependencies

### Assumptions
1. Users will grant all required Android permissions
2. Target devices support CallScreeningService API
3. Users have basic smartphone literacy

### Dependencies
1. Android CallScreeningService API availability
2. Device manufacturer's phone app behavior
3. SMS app permissions (varies by Android version)

---

## 9. Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Android permission changes | Medium | High | Abstract permission handling; monitor Android updates |
| Some OEMs blocking background services | High | High | Provide manufacturer-specific instructions |
| User grants partial permissions | Medium | Medium | Graceful degradation; clear permission prompts |
| Competitor apps with more features | Low | Medium | Focus on simplicity and reliability |

---

## 10. Timeline (Proposed)

| Phase | Duration | Deliverables |
|-------|----------|-------------|
| Design & Architecture | 2 weeks | UI designs, technical architecture |
| Core Development | 4 weeks | Call blocking, SMS blocking, whitelist management |
| Testing & QA | 2 weeks | Unit tests, integration tests, beta testing |
| Launch Preparation | 1 week | Play Store assets, documentation |
| **Total** | **9 weeks** | Production-ready v1.0 |

---

## 11. Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | | | |
| Tech Lead | | | |
| Design Lead | | | |
| QA Lead | | | |

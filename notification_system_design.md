# Campus Notification System

## Overview

The Campus Notification System sends important announcements to users such as students, staff, drivers, and administrators.

## Core Features

- Create and publish notifications.
- Support notification categories such as emergency, schedule update, general announcement, and maintenance.
- Send notifications to all users or selected user groups.
- Track notification status in memory or through an external service if required.

## Suggested API Endpoints

- `POST /notifications` - create a notification.
- `GET /notifications` - list notifications.
- `GET /notifications/{id}` - view one notification.
- `POST /notifications/{id}/send` - send a notification.

## Logging

All important operations must use the reusable Logging Middleware. Do not use `console.log`, `System.out`, or built-in loggers.

Example:

```text
Log("backend", "info", "service", "Campus emergency notification sent to all active users")
```

## Notes

- Use protected API calls with auth headers where required.
- Keep messages specific and contextual.
- Avoid storing unnecessary user data.

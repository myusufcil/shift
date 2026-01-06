import WidgetKit
import SQLite3

struct HabitProvider: TimelineProvider {

    func placeholder(in context: Context) -> HabitEntry {
        HabitEntry(date: Date(), habits: sampleHabits)
    }

    func getSnapshot(in context: Context, completion: @escaping (HabitEntry) -> Void) {
        let entry = HabitEntry(date: Date(), habits: loadHabits())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<HabitEntry>) -> Void) {
        let habits = loadHabits()
        let entry = HabitEntry(date: Date(), habits: habits)

        // Update every 30 minutes
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 30, to: Date())!
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
        completion(timeline)
    }

    private func loadHabits() -> [WidgetHabit] {
        guard let dbPath = getDatabasePath() else {
            return []
        }

        var db: OpaquePointer?
        guard sqlite3_open(dbPath, &db) == SQLITE_OK else {
            return []
        }
        defer { sqlite3_close(db) }

        var habits: [WidgetHabit] = []
        let today = getTodayString()

        // Get habits
        let habitQuery = "SELECT id, name, icon, color FROM Habit WHERE is_archived = 0 LIMIT 5"
        var statement: OpaquePointer?

        if sqlite3_prepare_v2(db, habitQuery, -1, &statement, nil) == SQLITE_OK {
            while sqlite3_step(statement) == SQLITE_ROW {
                let id = String(cString: sqlite3_column_text(statement, 0))
                let name = String(cString: sqlite3_column_text(statement, 1))
                let icon = String(cString: sqlite3_column_text(statement, 2))
                let color = String(cString: sqlite3_column_text(statement, 3))

                // Check completion status
                let isCompleted = checkCompletion(db: db, habitId: id, date: today)

                habits.append(WidgetHabit(
                    id: id,
                    name: name,
                    icon: getIconEmoji(icon),
                    color: color,
                    isCompleted: isCompleted
                ))
            }
        }
        sqlite3_finalize(statement)

        return habits
    }

    private func checkCompletion(db: OpaquePointer?, habitId: String, date: String) -> Bool {
        let query = "SELECT is_completed FROM HabitCompletion WHERE habit_id = ? AND date = ?"
        var statement: OpaquePointer?
        var isCompleted = false

        if sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (habitId as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 2, (date as NSString).utf8String, -1, nil)

            if sqlite3_step(statement) == SQLITE_ROW {
                isCompleted = sqlite3_column_int64(statement, 0) == 1
            }
        }
        sqlite3_finalize(statement)

        return isCompleted
    }

    private func getDatabasePath() -> String? {
        // Try App Group container first (for sharing with main app)
        if let groupURL = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: "group.com.cil.shift"
        ) {
            let dbPath = groupURL.appendingPathComponent("shift_v4.db").path
            if FileManager.default.fileExists(atPath: dbPath) {
                return dbPath
            }
        }

        // Fallback to main app's Documents directory
        guard let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return nil
        }

        let dbPath = documentsPath.appendingPathComponent("shift_v4.db").path
        if FileManager.default.fileExists(atPath: dbPath) {
            return dbPath
        }

        // Try Library directory (where SQLite might store it by default)
        guard let libraryPath = FileManager.default.urls(for: .libraryDirectory, in: .userDomainMask).first else {
            return nil
        }

        let libraryDbPath = libraryPath.appendingPathComponent("shift_v4.db").path
        if FileManager.default.fileExists(atPath: libraryDbPath) {
            return libraryDbPath
        }

        return nil
    }

    private func getTodayString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date())
    }

    private func getIconEmoji(_ icon: String) -> String {
        let lowercased = icon.lowercased()
        switch lowercased {
        case "water", "wat", "hydration": return "sf:drop.fill"
        case "vegetables", "veg": return "sf:leaf.fill"
        case "fruit", "fru": return "sf:apple.logo"
        case "cooking", "coo": return "sf:fork.knife"
        case "pill", "med": return "sf:pills.fill"
        case "journal", "jou": return "sf:pencil.line"
        case "meditation", "me": return "sf:figure.mind.and.body"
        case "books", "book", "boo", "read": return "sf:book.fill"
        case "running", "run": return "sf:figure.run"
        case "walking", "wal": return "sf:figure.walk"
        case "gym", "dumbbell", "dum", "fitness", "workout": return "sf:dumbbell.fill"
        case "yoga", "yog": return "sf:figure.yoga"
        case "sleep", "sle": return "sf:moon.zzz.fill"
        case "coffee", "cof": return "sf:cup.and.saucer.fill"
        case "music", "mus": return "sf:music.note"
        case "art", "palette": return "sf:paintpalette.fill"
        case "fire", "fir": return "sf:flame.fill"
        case "check", "che": return "sf:checkmark.circle.fill"
        default:
            // Check if it's already an emoji
            if icon.unicodeScalars.first?.properties.isEmoji == true {
                return icon
            }
            return "sf:checkmark.circle.fill"
        }
    }

    private var sampleHabits: [WidgetHabit] {
        [
            WidgetHabit(id: "1", name: "Drink Water", icon: "sf:drop.fill", color: "#00D9FF", isCompleted: true),
            WidgetHabit(id: "2", name: "Exercise", icon: "sf:dumbbell.fill", color: "#FF6B6B", isCompleted: false),
            WidgetHabit(id: "3", name: "Read Book", icon: "sf:book.fill", color: "#4ECDC4", isCompleted: false)
        ]
    }
}

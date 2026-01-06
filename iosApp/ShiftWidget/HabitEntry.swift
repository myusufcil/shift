import WidgetKit

struct HabitEntry: TimelineEntry {
    let date: Date
    let habits: [WidgetHabit]
}

struct WidgetHabit: Identifiable {
    let id: String
    let name: String
    let icon: String
    let color: String
    let isCompleted: Bool
}

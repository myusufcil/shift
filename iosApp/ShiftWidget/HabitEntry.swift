import WidgetKit

struct HabitEntry: TimelineEntry {
    let date: Date
    let habits: [WidgetHabit]

    var completedCount: Int {
        habits.filter { $0.isCompleted }.count
    }

    var progressPercent: Int {
        guard !habits.isEmpty else { return 0 }
        return (completedCount * 100) / habits.count
    }
}

struct WidgetHabit: Identifiable {
    let id: String
    let name: String
    let icon: String
    let color: String
    let isCompleted: Bool
    let streak: Int
    let currentValue: Int
    let targetValue: Int?

    init(id: String, name: String, icon: String, color: String, isCompleted: Bool, streak: Int = 0, currentValue: Int = 0, targetValue: Int? = nil) {
        self.id = id
        self.name = name
        self.icon = icon
        self.color = color
        self.isCompleted = isCompleted
        self.streak = streak
        self.currentValue = currentValue
        self.targetValue = targetValue
    }
}

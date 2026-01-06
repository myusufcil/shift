import WidgetKit
import SwiftUI

@main
struct ShiftWidget: Widget {
    let kind: String = "ShiftWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: HabitProvider()) { entry in
            ShiftWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Shift Habits")
        .description("Track and complete your daily habits quickly.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

struct ShiftWidgetEntryView: View {
    var entry: HabitEntry
    @Environment(\.widgetFamily) var family

    var body: some View {
        ZStack {
            Color(hex: "1A1A2E")

            VStack(alignment: .leading, spacing: 8) {
                // Header
                HStack {
                    Text("Shift")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: "00D9FF"))

                    if family != .systemSmall {
                        Text("Today's Habits")
                            .font(.caption)
                            .foregroundColor(Color(hex: "AAAAAA"))
                    }

                    Spacer()
                }
                .padding(.bottom, 4)

                if entry.habits.isEmpty {
                    emptyStateView
                } else {
                    habitListView
                }
            }
            .padding(12)
        }
        .containerBackground(Color(hex: "1A1A2E"), for: .widget)
    }

    private var emptyStateView: some View {
        VStack(spacing: 4) {
            Spacer()
            Text("No habits yet")
                .font(.subheadline)
                .foregroundColor(Color(hex: "AAAAAA"))
            Text("Tap to add habits")
                .font(.caption)
                .foregroundColor(Color(hex: "00D9FF"))
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }

    private var habitListView: some View {
        VStack(spacing: 6) {
            ForEach(entry.habits.prefix(maxHabits)) { habit in
                HabitRowView(habit: habit)
            }
            Spacer(minLength: 0)
        }
    }

    private var maxHabits: Int {
        switch family {
        case .systemSmall: return 3
        case .systemMedium: return 3
        case .systemLarge: return 6
        @unknown default: return 3
        }
    }
}

struct HabitRowView: View {
    let habit: WidgetHabit

    var body: some View {
        HStack(spacing: 8) {
            // Checkbox
            ZStack {
                RoundedRectangle(cornerRadius: 6)
                    .fill(habit.isCompleted ? Color(hex: "4ECDC4") : Color(hex: "333355"))
                    .frame(width: 24, height: 24)

                if habit.isCompleted {
                    Image(systemName: "checkmark")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                }
            }

            // Icon (SF Symbol or emoji)
            if habit.icon.hasPrefix("sf:") {
                Image(systemName: String(habit.icon.dropFirst(3)))
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "00D9FF"))
            } else {
                Text(habit.icon)
                    .font(.system(size: 16))
            }

            // Name
            Text(habit.name)
                .font(.subheadline)
                .foregroundColor(.white)
                .lineLimit(1)

            Spacer()
        }
        .padding(8)
        .background(Color(hex: "16213E"))
        .cornerRadius(10)
    }
}

// MARK: - Preview
struct ShiftWidget_Previews: PreviewProvider {
    static var previews: some View {
        ShiftWidgetEntryView(entry: HabitEntry(
            date: Date(),
            habits: [
                WidgetHabit(id: "1", name: "Drink Water", icon: "sf:drop.fill", color: "#00D9FF", isCompleted: true),
                WidgetHabit(id: "2", name: "Exercise", icon: "sf:dumbbell.fill", color: "#FF6B6B", isCompleted: false),
                WidgetHabit(id: "3", name: "Read Book", icon: "sf:book.fill", color: "#4ECDC4", isCompleted: true)
            ]
        ))
        .previewContext(WidgetPreviewContext(family: .systemMedium))
    }
}

// MARK: - Color Extension
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

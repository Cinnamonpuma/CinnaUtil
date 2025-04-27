# 🌟 CinnaUtil - A Meteor Addon for Minecraft 1.21.5

**CinnaUtil** is a modular utility addon for the [Meteor Client](https://meteorclient.com/), designed to enhance your gameplay experience with powerful features for automation, multi-instance control, and more. Built for **Minecraft 1.21.5**, it offers several unique modules and commands to supercharge your Meteor setup.

---

## 🚀 Features

### 📦 Modules

#### 🔁 `DupeSequencesModule`
- **Purpose:** Create and manage automated sequences for item duplication
- **Features:**
  - Custom sequence creation
  - Multiple action types: commands, packets, waits
  - Adjustable delays and repeat counts
  - GUI-based sequence editor
  - Save/load sequences
- **Category:** `CinnaUtil`

#### 🧠 `MultiInstanceCommand`
- **Purpose:** Advanced multi-instance control and automation
- **Features:**
  - Command synchronization
  - Automated sequence execution
  - Start/stop/toggle functionality
- **Category:** `CinnaUtil`

#### 🔄 `MultiInstanceMovement`
- **Purpose:** Synchronize actions between Minecraft instances
- **Features:**
  - Parent/Child mode system
  - Movement synchronization
  - Rotation syncing
  - Action mirroring
  - Network communication
- **Category:** `CinnaUtil`

#### 🎨 `ChatColorModule`
- **Purpose:** Enhanced chat customization
- **Features:**
  - Custom color schemes
  - Message formatting
  - Visual improvements
- **Category:** `CinnaUtil`

#### 💬 `ChatSyncModule`
- **Purpose:** Synchronize chat across instances
- **Features:**
  - Message synchronization
  - Command integration
  - Toggle functionality
- **Category:** `CinnaUtil`

---

## 🧾 Commands

#### ⏳ `WaitCommand`
- **Usage:** `.wait <ticks> <command>`
- **Purpose:** Execute commands with precise timing
- **Features:**
  - Tick-based timing system
  - Command queueing
  - Minecraft-synchronized execution

#### 🖥️ `MultiInstanceCmd`
- **Usage:** `.multidupe <start|stop|toggle>`
- **Purpose:** Control multi-instance functionality
- **Features:**
  - Start/stop automation
  - Module toggling
  - Status feedback

#### 💭 `ChatSyncCmd`
- **Usage:** `.chatsync <send|toggle>`
- **Purpose:** Control chat synchronization
- **Features:**
  - Message broadcasting
  - Module toggling
  - Status feedback

---

## 📦 Installation

1. Download the latest release of CinnaUtil for Minecraft 1.21.5
2. Place the `.jar` file in your Mods folder along with Meteor Client
3. Launch Meteor Client and locate modules under the `CinnaUtil` category

---

## 🛠️ Requirements

- Minecraft **1.21.5**
- [Meteor Client](https://meteorclient.com/)

---

## 🔧 Configuration

Access module settings through Meteor Client's GUI under the **CinnaUtil** category.

**DupeSequences Configuration:**
- Create custom sequences
- Set delays and repeat counts
- Configure action types
- Save/load configurations

**MultiInstance Configuration:**
- Set Parent/Child modes
- Configure network settings
- Customize sync options

---

## 📬 Feedback & Issues

Report issues or suggest features through GitHub issues or contact the developer directly.

---

## 🧠 Credits

Developed by Cinnamonpuma
Powered by Meteor Client

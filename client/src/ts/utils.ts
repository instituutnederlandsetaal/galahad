import type { Period } from "@/types/taggers"

// https://stackoverflow.com/a/18650828
export function formatBytes(bytes: number, decimals = 0) {
    if (!+bytes || bytes < 1023) return "> 1 kB"
    const dm = 0 > decimals ? 0 : decimals
    const d = Math.floor(Math.log(bytes) / Math.log(1024))
    const sizes = ["B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
    return `${Number.parseFloat((bytes / 1024 ** d).toFixed(dm))} ${sizes[d]}`
}

export function formatDate(unixtime: number) {
    if (unixtime <= 0) {
        return "Never"
    }
    const d = new Date(unixtime)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, "0")
    const day = String(d.getDate()).padStart(2, "0")
    const hours = String(d.getHours()).padStart(2, "0")
    const minutes = String(d.getMinutes()).padStart(2, "0")
    return `${year}-${month}-${day} ${hours}:${minutes}`
}

export function formatPeriod(period: Period): string | undefined {
    if (period) {
        const from = period.from ?? 0
        const to = period.to ?? 0
        return `${from} – ${to}`
    } else {
        return undefined
    }
}

// Small dependency-free CSV export helper used by the "Export CSV" button
// on the Expenses page. Kept generic so it can be reused for other tables
// (budgets, categories) later.
export function downloadCsv(filename: string, headers: string[], rows: (string | number)[][]) {
  const escapeCell = (cell: string | number) => {
    const str = String(cell ?? '')
    if (str.includes(',') || str.includes('"') || str.includes('\n')) {
      return `"${str.replace(/"/g, '""')}"`
    }
    return str
  }

  const lines = [headers.map(escapeCell).join(','), ...rows.map((row) => row.map(escapeCell).join(','))]
  const csvContent = lines.join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
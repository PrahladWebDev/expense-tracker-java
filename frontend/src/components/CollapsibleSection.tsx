import { useState, type ReactNode } from 'react'

interface CollapsibleSectionProps {
  title: string
  children: ReactNode
}

/**
 * On a phone, a group page stacks Members / Activity / Settlement history
 * on top of the expense list, pushing it far down the screen. This makes
 * each one a collapsible panel - closed by default, toggled with an
 * up/down arrow - so the header row is all you see until you tap it open.
 * On desktop (md and up) there's plenty of width to spare, so the content
 * always renders open and the arrow is hidden.
 */
export default function CollapsibleSection({ title, children }: CollapsibleSectionProps) {
  const [open, setOpen] = useState(false)

  return (
    <div>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-expanded={open}
        className="w-full flex items-center justify-between mb-3 md:cursor-default"
      >
        <h2 className="font-semibold text-gray-900">{title}</h2>
        <span aria-hidden className="md:hidden text-gray-400 text-xs">
          {open ? '▲' : '▼'}
        </span>
      </button>
      <div className={open ? 'block' : 'hidden md:block'}>{children}</div>
    </div>
  )
}

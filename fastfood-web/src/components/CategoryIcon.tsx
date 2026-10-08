/** Pictogramme simple par catégorie, en attendant de vraies photos des plats. */
export function CategoryIcon({ category, className = '' }: { category: string; className?: string }) {
  const common = {
    className,
    viewBox: '0 0 48 48',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 2.5,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
    'aria-hidden': true,
  }

  switch (category) {
    case 'Burgers':
      return (
        <svg {...common}>
          <path d="M8 20a16 12 0 0 1 32 0z" />
          <path d="M7 26h34" />
          <path d="M8 31h32v1a4 4 0 0 1-4 4H12a4 4 0 0 1-4-4z" />
        </svg>
      )
    case 'Accompagnements':
      return (
        <svg {...common}>
          <path d="M12 20h24l-3 20H15z" />
          <path d="M17 20l-2-10M22 20l-1-12M27 20l1-12M32 20l2-10" />
        </svg>
      )
    case 'Boissons':
      return (
        <svg {...common}>
          <path d="M14 14h20l-3 26H17z" />
          <path d="M26 14l3-8h5" />
          <path d="M15 22h18" />
        </svg>
      )
    default:
      return (
        <svg {...common}>
          <path d="M14 24h20l-4 16H18z" />
          <path d="M14 24a10 10 0 0 1 20 0" />
          <path d="M24 14v-6" />
        </svg>
      )
  }
}

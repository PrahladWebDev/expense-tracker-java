/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // CONCEPT: CSS-variable-driven brand palette
        // brand-600 etc. resolve to `rgb(var(--brand-600) / <alpha-value>)`
        // instead of a fixed hex. index.css defines the actual RGB triplets
        // per accent theme (default/nord/dracula/synthwave/forest) under
        // [data-accent="..."] selectors, so switching the `data-accent`
        // attribute on <html> (see ThemeProvider) re-colors every
        // `bg-brand-*` / `text-brand-*` / `border-brand-*` utility already
        // used throughout the app - buttons, links, active nav state,
        // badges - without touching those component files individually.
        brand: {
          50: "rgb(var(--brand-50) / <alpha-value>)",
          100: "rgb(var(--brand-100) / <alpha-value>)",
          300: "rgb(var(--brand-300) / <alpha-value>)",
          500: "rgb(var(--brand-500) / <alpha-value>)",
          600: "rgb(var(--brand-600) / <alpha-value>)",
          700: "rgb(var(--brand-700) / <alpha-value>)",
        },
      },
    },
  },
  plugins: [],
}

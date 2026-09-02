import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import hi from './locales/hi.json'

// CONCEPT: i18next + react-i18next
// i18next is the actual translation engine (holds the loaded resource
// bundles, does key lookup + {{variable}} interpolation, tracks the
// current language). react-i18next is a thin binding on top of it that
// gives components the `useTranslation()` hook - so `t('settlement.owes',
// { amount })` re-renders automatically whenever the language changes,
// without any component needing to know HOW the translation is stored.
//
// SCOPE NOTE: this covers the app's navigation and the group
// settlement/balance strings (the ones with genuinely different word
// order between English and casual Hindi - see hi.json's `owes`/`isOwed`,
// where the amount moves to the front of the sentence). Extending this to
// every screen (Dashboard, Expenses, Budgets, Categories forms, etc.)
// would mean adding useTranslation() + resource keys to each of those
// pages individually - the same shape of change as here, just repeated
// per page - so it's a straightforward follow-up rather than something
// requiring new infrastructure.
export const LANGUAGE_STORAGE_KEY = 'expense_tracker_language'

function getInitialLanguage(): 'en' | 'hi' {
  const stored = localStorage.getItem(LANGUAGE_STORAGE_KEY)
  return stored === 'hi' ? 'hi' : 'en'
}

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    hi: { translation: hi },
  },
  lng: getInitialLanguage(),
  fallbackLng: 'en',
  interpolation: {
    escapeValue: false, // React already escapes output, so i18next doesn't need to
  },
});

i18n.on('languageChanged', (lng: string) => {
  localStorage.setItem(LANGUAGE_STORAGE_KEY, lng)
})

export default i18n

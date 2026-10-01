import React, { createContext, useContext, useState, useEffect } from 'react';

export const THEMES = {
  'cherry-cream': {
    id: 'cherry-cream',
    name: 'Cherry Cream',
    tagline: 'Luxury Academic',
    primaryColor: '#720921',
    accentColor: '#B82A45',
    bgColor: '#FBF9F5',
    surfaceColor: '#FFFFFF',
    textPrimary: '#1E1E24',
    textSecondary: '#665C54',
    sidebarBg: '#480514',
    sidebarActive: '#720921',
    chartColors: ['#720921', '#B82A45', '#C07D45', '#4A6B56', '#6B5876'],
    swatch: {
      primary: '#720921',
      bg: '#FBF9F5',
      accent: '#B82A45',
    },
  },
};

const ThemeContext = createContext(null);

export const ThemeProvider = ({ children }) => {
  const [theme, setThemeState] = useState(() => {
    const saved = localStorage.getItem('scms_theme');
    return (saved && THEMES[saved]) ? saved : 'cherry-cream';
  });

  const setTheme = (themeId) => {
    if (THEMES[themeId]) {
      setThemeState(themeId);
      localStorage.setItem('scms_theme', themeId);
    }
  };

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const currentTheme = THEMES[theme] || THEMES['cherry-cream'];

  return (
    <ThemeContext.Provider
      value={{
        theme,
        setTheme,
        currentTheme,
        themes: THEMES,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

export default ThemeContext;

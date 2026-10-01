import React, { useState, useRef, useEffect } from 'react';
import { useTheme, THEMES } from '../../context/ThemeContext';
import { FiCheck, FiMoon, FiSun, FiLayout } from 'react-icons/fi';

export const ThemeSelector = ({ variant = 'dropdown' }) => {
  const { theme, setTheme } = useTheme();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (variant === 'list') {
    return (
      <div className="space-y-2.5">
        {Object.values(THEMES).map((t) => {
          const isSelected = theme === t.id;
          return (
            <button
              key={t.id}
              onClick={() => setTheme(t.id)}
              className={`w-full flex items-center justify-between p-3 rounded-xl border text-left transition-all ${
                isSelected
                  ? 'border-primary ring-2 ring-primary/20 bg-theme-elevated shadow-sm'
                  : 'border-theme hover:bg-theme-elevated'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className="flex items-center -space-x-1">
                  <span
                    className="w-5 h-5 rounded-full border border-white shadow-xs"
                    style={{ backgroundColor: t.swatch.primary }}
                  />
                  <span
                    className="w-5 h-5 rounded-full border border-white shadow-xs"
                    style={{ backgroundColor: t.swatch.accent }}
                  />
                  <span
                    className="w-5 h-5 rounded-full border border-white shadow-xs"
                    style={{ backgroundColor: t.swatch.bg }}
                  />
                </div>
                <div>
                  <div className="text-sm font-semibold text-theme-primary">{t.name}</div>
                  <div className="text-xs text-theme-muted">{t.tagline}</div>
                </div>
              </div>
              {isSelected && <FiCheck className="w-5 h-5 text-theme-brand shrink-0" />}
            </button>
          );
        })}
      </div>
    );
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-2.5 py-1.5 rounded-lg text-xs font-semibold border border-theme hover:bg-theme-elevated text-theme-secondary hover:text-theme-primary transition-colors focus:outline-none"
        title="Change Color Theme"
        aria-label="Change Color Theme"
      >
        <span
          className="w-3.5 h-3.5 rounded-full border border-slate-300 shrink-0"
          style={{ backgroundColor: THEMES[theme]?.swatch.primary || '#720921' }}
        />
        <span className="hidden sm:inline">{THEMES[theme]?.name || 'Theme'}</span>
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 p-2 rounded-2xl shadow-xl bg-theme-surface border border-theme z-50 animate-in fade-in zoom-in-95 duration-150">
          <div className="px-3 py-2 border-b border-theme mb-1">
            <p className="text-xs font-bold text-theme-primary uppercase tracking-wider">
              Visual Theme
            </p>
            <p className="text-[11px] text-theme-muted">
              Choose your campus appearance
            </p>
          </div>
          <div className="space-y-1">
            {Object.values(THEMES).map((t) => {
              const isSelected = theme === t.id;
              return (
                <button
                  key={t.id}
                  onClick={() => {
                    setTheme(t.id);
                    setIsOpen(false);
                  }}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-left text-xs transition-colors ${
                    isSelected
                      ? 'bg-theme-primary-light font-bold text-theme-brand'
                      : 'hover:bg-theme-elevated text-theme-primary'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <div className="flex items-center -space-x-1">
                      <span
                        className="w-4 h-4 rounded-full border border-white"
                        style={{ backgroundColor: t.swatch.primary }}
                      />
                      <span
                        className="w-4 h-4 rounded-full border border-white"
                        style={{ backgroundColor: t.swatch.accent }}
                      />
                    </div>
                    <div>
                      <div className="font-semibold">{t.name}</div>
                      <div className="text-[10px] text-theme-muted font-normal">{t.tagline}</div>
                    </div>
                  </div>
                  {isSelected && <FiCheck className="w-4 h-4 text-theme-brand shrink-0" />}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default ThemeSelector;

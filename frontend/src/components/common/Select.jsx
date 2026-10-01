import React, { forwardRef } from 'react';

export const Select = forwardRef(({
  label,
  name,
  value,
  onChange,
  options = [],
  placeholder = 'Select an option',
  error,
  helperText,
  required = false,
  disabled = false,
  className = '',
  ...props
}, ref) => {
  return (
    <div className={`w-full ${className}`}>
      {label && (
        <label htmlFor={name} className="block text-xs font-semibold uppercase tracking-wider text-theme-secondary mb-1.5">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <div className="relative">
        <select
          ref={ref}
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          disabled={disabled}
          required={required}
          className={`block w-full rounded-lg border text-sm transition-all focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed px-3.5 py-2.5 bg-theme-surface ${
            error
              ? 'border-red-500 focus:border-red-500 focus:ring-red-400 text-red-900 bg-red-50/20 dark:text-red-300'
              : 'border-theme text-theme-primary focus:border-theme-primary focus:ring-theme hover:border-slate-400'
          }`}
          {...props}
        >
          {placeholder && (
            <option value="" disabled className="text-slate-500 bg-white dark:bg-slate-900">
              {placeholder}
            </option>
          )}
          {options.map((opt) => {
            const val = typeof opt === 'object' ? opt.value : opt;
            const lbl = typeof opt === 'object' ? opt.label : opt;
            return (
              <option key={val} value={val} className="bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100">
                {lbl}
              </option>
            );
          })}
        </select>
      </div>
      {error ? (
        <p className="mt-1 text-xs text-red-600 dark:text-red-400 font-medium">{error}</p>
      ) : helperText ? (
        <p className="mt-1 text-xs text-theme-muted">{helperText}</p>
      ) : null}
    </div>
  );
});

Select.displayName = 'Select';

export default Select;

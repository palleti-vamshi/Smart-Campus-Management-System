import React, { useEffect } from 'react';
import { FiX } from 'react-icons/fi';

export const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  maxWidth = 'max-w-xl',
  footer,
}) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="min-h-screen px-4 text-center">
        {/* Backdrop */}
        <div
          className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity"
          onClick={onClose}
        />

        {/* Center alignment trick */}
        <span className="inline-block h-screen align-middle" aria-hidden="true">
          &#8203;
        </span>

        {/* Modal dialog */}
        <div className={`inline-block w-full ${maxWidth} p-6 my-8 text-left align-middle transition-all transform bg-theme-surface shadow-2xl rounded-2xl border border-theme relative z-10`}>
          <div className="flex items-center justify-between pb-3.5 border-b border-theme-subtle">
            <h3 className="text-lg font-bold text-theme-primary tracking-tight">
              {title}
            </h3>
            <button
              onClick={onClose}
              className="text-theme-muted hover:text-theme-primary rounded-lg p-1.5 transition-colors focus:outline-none focus:ring-2 focus:ring-theme"
              aria-label="Close dialog"
            >
              <FiX className="w-5 h-5" />
            </button>
          </div>

          <div className="mt-4">{children}</div>

          {footer && (
            <div className="mt-6 pt-4 border-t border-theme-subtle flex items-center justify-end gap-3">
              {footer}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Modal;

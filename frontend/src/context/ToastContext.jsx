import React, { createContext, useContext, useState, useCallback } from 'react';
import { FiCheckCircle, FiAlertTriangle, FiAlertCircle, FiInfo, FiX } from 'react-icons/fi';

const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback((message, type = 'success', duration = 4000) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 5);
    setToasts((prev) => [...prev, { id, message, type }]);

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }
  }, [removeToast]);

  return (
    <ToastContext.Provider value={{ showToast, removeToast }}>
      {children}
      {/* Toast container */}
      <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none px-4 sm:px-0">
        {toasts.map((toast) => {
          const typeConfig = {
            success: {
              icon: FiCheckCircle,
              bg: 'bg-emerald-800 text-white border-emerald-700',
              iconColor: 'text-emerald-300',
            },
            error: {
              icon: FiAlertCircle,
              bg: 'bg-rose-900 text-white border-rose-800',
              iconColor: 'text-rose-300',
            },
            warning: {
              icon: FiAlertTriangle,
              bg: 'bg-amber-900 text-white border-amber-800',
              iconColor: 'text-amber-300',
            },
            info: {
              icon: FiInfo,
              bg: 'bg-slate-800 text-white border-slate-700',
              iconColor: 'text-cyan-300',
            },
          }[toast.type] || {
            icon: FiInfo,
            bg: 'bg-slate-800 text-white border-slate-700',
            iconColor: 'text-white',
          };

          const Icon = typeConfig.icon;

          return (
            <div
              key={toast.id}
              className={`pointer-events-auto flex items-start gap-3 p-3.5 rounded-xl shadow-xl border text-sm transition-all transform animate-in slide-in-from-bottom-3 duration-200 ${typeConfig.bg}`}
              role="alert"
            >
              <Icon className={`w-5 h-5 shrink-0 mt-0.5 ${typeConfig.iconColor}`} />
              <div className="flex-1 font-medium leading-snug">{toast.message}</div>
              <button
                onClick={() => removeToast(toast.id)}
                className="text-white/70 hover:text-white p-0.5 rounded transition-colors"
                aria-label="Close notification"
              >
                <FiX className="w-4 h-4" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

export default ToastContext;
